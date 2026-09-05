package org.mpashka.vocabulary.importer;

import org.mpashka.vocabulary.core.Chunk;
import org.mpashka.vocabulary.core.Entry;
import org.mpashka.vocabulary.core.EntryParser;
import org.mpashka.vocabulary.core.Gender;
import org.mpashka.vocabulary.core.MarkupParser;
import org.mpashka.vocabulary.core.PartOfSpeech;
import org.mpashka.vocabulary.core.Serbian;
import org.mpashka.vocabulary.importer.Homonyms.Homonym;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Переносит словарь из исходной sqlite-базы в целевую Postgres (этап 5 плана).
 *
 * <p>Что переносится: слово, значения, переводы, примеры, устойчивые обороты, словоформы
 * для поиска. У каждого слова проставляется состояние и, если остались известные
 * языковые пробелы, признак {@code needs_language_review} с причинами — оттачивание
 * качества отложено на потом (см. [docs/implementation/db-schema.md]).
 *
 * <p>Запуск: {@code ./gradlew :importer:migrate}
 * (по желанию два аргумента: путь к sqlite и строка подключения к Postgres).
 */
// @tag:import
public final class MigrateToPostgres {

    private static final String DEFAULT_PG =
            "jdbc:postgresql://localhost:5433/vocabulary?user=vocabulary&password=vocabulary";

    public static void main(String[] args) throws SQLException {
        String sqlitePath = args.length > 0 ? args[0] : SourceReader.DEFAULT_PATH;
        String pgUrl = args.length > 1 ? args[1] : DEFAULT_PG;

        List<SourceReader.Row> rows = new ArrayList<>(46_000);
        new SourceReader(sqlitePath).forEach(rows::add);
        System.out.printf("Прочитано из исходной базы: %d статей%n", rows.size());

        try (Connection pg = DriverManager.getConnection(pgUrl)) {
            pg.setAutoCommit(false);
            clear(pg);

            var counters = new Counters();
            try (var w = new Writers(pg)) {
                for (SourceReader.Row row : rows) {
                    migrate(row, w, counters);
                }
            }
            pg.commit();

            System.out.printf("%nПеренесено слов:        %d%n", counters.words);
            System.out.printf("Значений:               %d%n", counters.senses);
            System.out.printf("Переводов:              %d%n", counters.translations);
            System.out.printf("Примеров и оборотов:    %d%n", counters.examples);
            System.out.printf("Словоформ для поиска:    %d%n", counters.forms);
            System.out.printf("%nРазделено омонимов: %d статей -> %d слов%n",
                    counters.homonymEntries, counters.homonymWords);
            System.out.printf("%nТребуют языковой доработки: %d (%.1f%%)%n",
                    counters.needsReview, 100.0 * counters.needsReview / counters.words);
            System.out.printf("  часть речи не определена:  %d%n", counters.unknownPos);
            System.out.printf("  нет ударения:              %d%n", counters.noAccent);
            System.out.printf("%nВариантов ударения сохранено: %d%n", counters.accentVariants);
        }
    }

    private static void migrate(SourceReader.Row row, Writers w, Counters c) throws SQLException {
        List<Chunk> chunks = MarkupParser.parse(row.xml());
        if (chunks.isEmpty()) {
            return;
        }
        // Омонимы, упакованные в одну строку, разносим по отдельным словам: у каждого
        // своё ударение, своя часть речи и свои значения.
        List<Homonym> homonyms = Homonyms.split(row.xml());
        if (homonyms.size() >= 2) {
            c.homonymEntries++;
            int index = 1;
            for (Homonym homonym : homonyms) {
                migrateOne(row, homonym.markup(), homonym.numeral(), index++, w, c);
                c.homonymWords++;
            }
            return;
        }
        migrateOne(row, row.xml(), null, 1, w, c);
    }

    /** Переносит одну статью: либо целую, либо один омоним из упакованных в строку. */
    private static void migrateOne(SourceReader.Row row, String markup, String numeral,
                                   int homonymIndex, Writers w, Counters c) throws SQLException {
        List<Chunk> chunks = MarkupParser.parse(markup);
        if (chunks.isEmpty()) {
            return;
        }
        Entry entry = EntryParser.parse(row.name(), row.kw(), markup);
        Gender gender = Gender.fromMarks(entry.grammar());

        // Известные языковые пробелы — их и помечаем к доработке.
        List<String> reasons = new ArrayList<>();
        if (entry.partOfSpeech() == PartOfSpeech.UNKNOWN) {
            reasons.add("часть речи не определена");
            c.unknownPos++;
        }
        // Ударение отсутствует. У проклитик (за, из, и, да) его нет по природе,
        // у остальных это пробел данных — разделить одно от другого разбором нельзя,
        // поэтому помечаем все и разбираем на доработке.
        if (!hasAccent(entry.headword())) {
            reasons.add("нет ударения");
            c.noAccent++;
        }
        boolean hasTranslation = entry.senses().stream()
                .anyMatch(s -> !s.translations().isEmpty());

        String status = hasTranslation ? "IMPORTED" : "NO_TRANSLATION";
        long wordId = w.insertWord(entry, gender, status, row.name(), homonymIndex,
                reasons.isEmpty() ? null : String.join(", ", reasons));
        c.words++;
        if (!reasons.isEmpty()) {
            c.needsReview++;
        }

        int senseOrdinal = 0;
        for (Entry.Sense sense : entry.senses()) {
            long senseId = w.insertSense(wordId, sense.number(), senseOrdinal++);
            c.senses++;
            int trOrdinal = 0;
            for (String translation : sense.translations()) {
                w.insertTranslation(senseId, translation, trOrdinal++);
                c.translations++;
            }
            int exOrdinal = 0;
            for (Entry.Example example : sense.examples()) {
                w.insertExample(wordId, senseId, example, false, exOrdinal++);
                c.examples++;
            }
        }
        int idiomOrdinal = 0;
        for (Entry.Example idiom : entry.idioms()) {
            w.insertExample(wordId, null, idiom, true, idiomOrdinal++);
            c.examples++;
        }

        // Словоформы для поиска. Заглавная форма достоверна (из старого словаря),
        // остальные порождены правилами и пока без ударения.
        String headwordPlain = Serbian.stripCombiningAccents(entry.headword());
        w.insertForm(wordId, entry.headword(), headwordPlain, "nom.sg",
                "SOURCE_DICTIONARY", "SOURCE_DICTIONARY", true);
        c.forms++;
        // Варианты ударения заглавного слова. Словарь выписывает их подряд
        // (го‛ра ж., го“ра ж.) — это допустимые произношения одного слова, и оба
        // относятся к каждому омониму (сверено со словарём Толстого). Первый уже
        // записан как заглавный, остальные кладём отдельными формами.
        for (String variant : accentVariants(chunks)) {
            String rendered = Serbian.renderAccents(variant);
            if (!rendered.equals(entry.headword())) {
                w.insertForm(wordId, rendered, headwordPlain, "nom.sg.вариант",
                        "SOURCE_DICTIONARY", "SOURCE_DICTIONARY", false);
                c.forms++;
                c.accentVariants++;
            }
        }
        // Латиница без ударений — чтобы искать и латинскими буквами. В самом
        // headword_latin ударения стоят комбинируемыми знаками и мешают префиксу.
        String latinPlain = Serbian.toLatin(headwordPlain);
        if (!latinPlain.equals(headwordPlain)) {
            w.insertForm(wordId, null, latinPlain, "nom.sg.lat", "SOURCE_DICTIONARY", null, false);
            c.forms++;
        }
        for (String form : WordForms.searchForms(entry, gender, chunks)) {
            if (form.equals(headwordPlain)) {
                continue;
            }
            w.insertForm(wordId, null, form, null, "RULES", null, false);
            c.forms++;
        }
    }

    /**
     * Римские цифры омонимов, упакованных в одну строку.
     *
     * <p>Признак точный: <b>повтор заглавного слова</b> (фрагмент {@code C}, равный
     * заглавному слову) и следом за ним, в пределах шапки статьи, римская цифра.
     *
     * <pre>
     *   би“ти$C#I$#,#бу“де_м$C#…  би“ти$C#II$#,#би“је_м$C#…   → [I, II]
     * </pre>
     *
     * <p>Между повтором и цифрой могут стоять грамматические пометы
     * ({@code го‛ра$C#ж$#.#,#го“ра$C#ж$#.#I$#}), поэтому цифру ищем не строго
     * следующим фрагментом, а до первого перевода или номера значения.
     *
     * <p>Требование повтора заглавного слова отсекает ссылки на чужие омонимы
     * («см. Ера II»): там цифра стоит после ссылки, а не после заглавного слова.
     * Проверено: широкий признак «любая римская цифра» давал 177 ложных срабатываний.
     */
    private static List<String> homonymNumerals(List<Chunk> chunks) {
        String headword = plain(chunks.getFirst().text());
        List<String> numerals = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (!Chunk.SERBIAN.equals(chunk.tag()) || !plain(chunk.text()).equals(headword)) {
                continue;
            }
            for (int j = i + 1; j < chunks.size() && j < i + 7; j++) {
                Chunk next = chunks.get(j);
                if (next.isTranslation() || Chunk.SENSE_NUMBER.equals(next.tag())) {
                    break;
                }
                if (Chunk.RUSSIAN_PLAIN.equals(next.tag()) && isRomanNumeral(next.text().trim())) {
                    numerals.add(next.text().trim());
                    break;
                }
            }
        }
        return numerals;
    }

    /** Есть ли в отрисованном слове хоть один знак тона (не считая долготы). */
    private static boolean hasAccent(String rendered) {
        for (int i = 0; i < rendered.length(); i++) {
            char ch = rendered.charAt(i);
            if (ch == '\u0300' || ch == '\u0301' || ch == '\u030F' || ch == '\u0311') {
                return true;
            }
        }
        return false;
    }

    /**
     * Варианты ударения заглавного слова: повторы заглавного слова в шапке статьи.
     * Словарь пишет их подряд через запятую — это допустимые произношения одного слова.
     */
    private static List<String> accentVariants(List<Chunk> chunks) {
        String headword = plain(chunks.getFirst().text());
        List<String> variants = new ArrayList<>();
        for (Chunk chunk : chunks) {
            if (chunk.isTranslation() || Chunk.SENSE_NUMBER.equals(chunk.tag())) {
                break;
            }
            if (Chunk.SERBIAN.equals(chunk.tag()) && plain(chunk.text()).equals(headword)) {
                String variant = Serbian.stripStemMarker(chunk.text());
                if (!variants.contains(variant)) {
                    variants.add(variant);
                }
            }
        }
        return variants;
    }

    private static String plain(String text) {
        return Serbian.stripAccents(Serbian.stripStemMarker(text)).trim();
    }

    private static boolean isRomanNumeral(String text) {
        return text.matches("I{1,3}|IV|VI{0,3}");
    }

    private static void clear(Connection pg) throws SQLException {
        try (Statement st = pg.createStatement()) {
            st.execute("truncate word restart identity cascade");
        }
    }

    /** Подготовленные запросы вставки, живут на время переноса. */
    private static final class Writers implements AutoCloseable {
        private final PreparedStatement word;
        private final PreparedStatement sense;
        private final PreparedStatement translation;
        private final PreparedStatement example;
        private final PreparedStatement form;

        Writers(Connection pg) throws SQLException {
            word = pg.prepareStatement("""
                    insert into word (headword, headword_plain, headword_latin,
                        part_of_speech, part_of_speech_source, gender, status,
                        last_changed_by, source_key, homonym_index,
                        needs_language_review, review_reason)
                    values (?, ?, ?, cast(? as part_of_speech), 'RULES',
                        cast(? as gender), cast(? as word_status), 'RULES', ?, ?, ?, ?)
                    returning id""");
            sense = pg.prepareStatement(
                    "insert into sense (word_id, number, source, ordinal) "
                            + "values (?, ?, 'SOURCE_DICTIONARY', ?) returning id");
            translation = pg.prepareStatement(
                    "insert into translation (sense_id, text, source, ordinal) "
                            + "values (?, ?, 'SOURCE_DICTIONARY', ?)");
            example = pg.prepareStatement(
                    "insert into example (word_id, sense_id, serbian, russian, is_idiom, "
                            + "source, ordinal) values (?, ?, ?, ?, ?, 'SOURCE_DICTIONARY', ?)");
            form = pg.prepareStatement(
                    "insert into word_form (word_id, form, form_plain, grammar, source, "
                            + "accent_source, is_preferred) values (?, ?, ?, ?, "
                            + "cast(? as data_source), cast(? as data_source), ?)");
        }

        long insertWord(Entry entry, Gender gender, String status, String sourceKey,
                        int homonymIndex, String reason) throws SQLException {
            word.setString(1, entry.headword());
            word.setString(2, Serbian.stripCombiningAccents(entry.headword()));
            word.setString(3, entry.headwordLatin());
            word.setString(4, entry.partOfSpeech().name());
            word.setString(5, gender == null ? null : gender.name());
            word.setString(6, status);
            word.setString(7, sourceKey);
            word.setInt(8, homonymIndex);
            word.setBoolean(9, reason != null);
            word.setString(10, reason);
            return returningId(word);
        }

        long insertSense(long wordId, Integer number, int ordinal) throws SQLException {
            sense.setLong(1, wordId);
            if (number == null) {
                sense.setNull(2, java.sql.Types.INTEGER);
            } else {
                sense.setInt(2, number);
            }
            sense.setInt(3, ordinal);
            return returningId(sense);
        }

        void insertTranslation(long senseId, String text, int ordinal) throws SQLException {
            translation.setLong(1, senseId);
            translation.setString(2, text);
            translation.setInt(3, ordinal);
            translation.executeUpdate();
        }

        void insertExample(long wordId, Long senseId, Entry.Example ex, boolean idiom,
                           int ordinal) throws SQLException {
            example.setLong(1, wordId);
            if (senseId == null) {
                example.setNull(2, java.sql.Types.BIGINT);
            } else {
                example.setLong(2, senseId);
            }
            example.setString(3, ex.serbian());
            example.setString(4, ex.russian());
            example.setBoolean(5, idiom);
            example.setInt(6, ordinal);
            example.executeUpdate();
        }

        void insertForm(long wordId, String withAccent, String plain, String grammar,
                       String source, String accentSource, boolean preferred) throws SQLException {
            form.setLong(1, wordId);
            form.setString(2, withAccent);
            form.setString(3, plain);
            form.setString(4, grammar);
            form.setString(5, source);
            form.setString(6, accentSource);
            form.setBoolean(7, preferred);
            form.executeUpdate();
        }

        private static long returningId(PreparedStatement statement) throws SQLException {
            try (ResultSet keys = statement.executeQuery()) {
                keys.next();
                return keys.getLong(1);
            }
        }

        @Override
        public void close() throws SQLException {
            for (PreparedStatement st : List.of(word, sense, translation, example, form)) {
                st.close();
            }
        }
    }

    private static final class Counters {
        int words;
        int senses;
        int translations;
        int examples;
        int forms;
        int needsReview;
        int homonymEntries;
        int homonymWords;
        int unknownPos;
        int noAccent;
        int accentVariants;
    }

    private MigrateToPostgres() {
    }
}
