package org.mpashka.vocabulary.backend;

import org.mpashka.vocabulary.core.Form;
import org.mpashka.vocabulary.core.Serbian;
import org.mpashka.vocabulary.core.Gender;
import org.mpashka.vocabulary.core.NounDeclension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Чтение словаря из целевой базы Postgres.
 *
 * <p>Пришло на смену чтению из исходной sqlite: словарь перенесён в Postgres
 * (см. {@code importer:migrate}). Ответы API при этом не изменились.
 */
// @tag:source-db
@Repository
public class PostgresDictionary {

    private final JdbcTemplate jdbc;

    public PostgresDictionary(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Условие совпадения словоформы: сама форма либо её латинская запись.
     * Два знака вопроса — один и тот же образец дважды.
     */
    private static final String FORM_MATCHES = """
            (f.form_plain like ?
              or lower(translate(replace(replace(replace(f.form_plain,
                  'љ', 'lj'), 'њ', 'nj'), 'џ', 'dž'),
                  'абвгдђежзијклмнопрстћуфхцчш',
                  'abvgdđežzijklmnoprstćufhcčš')) like ?)
            """;

    /**
     * Поиск слов. Ищет по началу заглавного слова (латиница и кириллица),
     * по русскому переводу и — если это включено — <b>по любой словоформе</b>.
     *
     * <p>Поиск по формам находит слово, которого в запросе нет ни одной буквой из
     * заглавной строки ({@code вода} находит {@code во̏д}), поэтому он выключаем:
     * это разные способы искать, а не улучшение одного.
     *
     * @param matchForms искать ли по словоформам
     */
    public List<FoundWord> search(String query, int limit, String alphabet, boolean matchForms) {
        String normalized = query.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return List.of();
        }
        String prefix = normalized + "%";
        boolean cyrillic = normalized.codePoints()
                .anyMatch(codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.CYRILLIC);
        String serbian = matchForms
                ? """
                w.headword_plain like ?
                   or lower(w.headword_latin) like ?
                   or exists (select 1 from word_form f
                              where f.word_id = w.id and """ + FORM_MATCHES + ")"
                : """
                w.headword_plain like ?
                   or lower(w.headword_latin) like ?
                """;
        String russian = """
                exists (select 1 from translation t
                        join sense s on s.id = t.sense_id
                        where s.word_id = w.id and lower(t.text) like ?)
                """;
        // Какие половины условия участвуют, решается один раз: и текст запроса, и его
        // параметры собираются по этим двум признакам. Порознь они разъезжаются —
        // на латинском запросе так и вышло: параметров было на один больше, чем
        // знаков вопроса, и поиск отвечал ошибкой 500.
        boolean withSerbian = !("separate".equals(alphabet) && cyrillic);
        boolean withRussian = "any".equals(alphabet) || cyrillic;
        String where = withSerbian && withRussian
                ? "(" + serbian + " or " + russian + ")"
                : withSerbian ? serbian : russian;
        // distinct: одно слово может совпасть сразу по нескольким словоформам.
        String sql = """
                select w.id, w.headword, w.headword_plain, w.headword_latin,
                       w.part_of_speech, w.status
                from word w
                where %s
                order by
                    case when w.headword_plain like ? then 0 else 1 end,
                    length(w.headword_plain), w.headword_plain
                limit ?
                """.formatted(where);
        List<Object> arguments = new ArrayList<>();
        if (withSerbian) {
            arguments.add(prefix);
            arguments.add(prefix);
            if (matchForms) {
                arguments.add(prefix);
                arguments.add(prefix);
            }
        }
        if (withRussian) {
            arguments.add("%" + normalized + "%");
        }
        arguments.add(prefix);
        arguments.add(limit);
        List<FoundWord> found = jdbc.query(sql,
                (rs, i) -> new FoundWord(rs.getLong("id"), rs.getString("headword"),
                        rs.getString("headword_plain"), rs.getString("headword_latin"),
                        rs.getString("part_of_speech"), rs.getString("status"), List.of(), null),
                arguments.toArray());

        return attachTranslations(attachMatchedForms(found, prefix, matchForms));
    }

    /**
     * Дописывает к найденному слову ту форму, по которой оно нашлось.
     *
     * <p>Только тем словам, которые <b>без указателя форм не нашлись бы вовсе</b>: если
     * запросу отвечает само заглавное слово, объяснять нечего. Иначе пользователь видит
     * {@code во̏д} в ответ на «вода» и не понимает, как он там оказался.
     *
     * <p>Заглавное слово сравнивается <b>без знаков ударения</b>, и кириллическое, и
     * латинское: в {@code headword_latin} ударение стоит комбинируемым знаком
     * ({@code vòda}), поэтому обычное сравнение с началом запроса {@code voda} не
     * срабатывает, и объяснение приписывалось слову, которое и так нашлось.
     */
    // @tag:word-forms
    private List<FoundWord> attachMatchedForms(List<FoundWord> found, String prefix, boolean matchForms) {
        if (!matchForms) {
            return found;
        }
        String plainPrefix = prefix.substring(0, prefix.length() - 1);
        List<FoundWord> result = new ArrayList<>(found.size());
        for (FoundWord word : found) {
            if (startsWithPlain(word.headwordPlain(), plainPrefix)
                    || startsWithPlain(word.headwordLatin(), plainPrefix)) {
                result.add(word);
                continue;
            }
            // Ярлык `.lat` носит латинская запись заглавного слова — это не словоформа.
            List<Form> matched = jdbc.query("""
                            select grammar, coalesce(form, form_plain) as form from word_form f
                            where f.word_id = ? and coalesce(f.grammar, '') not like '%.lat'
                              and """ + FORM_MATCHES + """
                            order by length(f.form_plain), f.grammar
                            limit 1
                            """,
                    (rs, i) -> new Form(rs.getString("grammar"), rs.getString("form")),
                    word.id(), prefix, prefix);
            result.add(word.withMatchedForm(matched.isEmpty() ? null : matched.getFirst()));
        }
        return result;
    }

    /** Начинается ли слово с запроса, если ударения не считать. */
    private static boolean startsWithPlain(String word, String plainPrefix) {
        return word != null
                && Serbian.stripCombiningAccents(word).toLowerCase().startsWith(plainPrefix);
    }

    private static final String WORD_COLUMNS = """
            select id, headword, headword_plain, headword_latin,
                   part_of_speech, status, needs_language_review, review_reason,
                   homonym_index
            from word w
            """;

    /**
     * Статьи по написанию слова.
     *
     * <p>Заглавное слово <b>сильнее словоформы</b>: пока есть статья с таким написанием,
     * поиск по формам не включается вовсе. Иначе «вода» приводит к {@code во̏д}, у
     * которого «вода» — родительный падеж, и два разных слова показываются как омонимы
     * одного.
     *
     * <p>По заглавному слову находятся настоящие омонимы — самостоятельные слова с одним
     * написанием ({@code бити} — «быть» и «бить»). По форме находятся <b>разные</b> слова,
     * и {@code matchedBy} говорит, какой это случай.
     */
    public Lookup lookupByName(String name) {
        String plain = Serbian.stripCombiningAccents(name).toLowerCase();
        List<WordRow> exact = jdbc.query(
                WORD_COLUMNS + "where w.headword_plain = ? order by homonym_index",
                WORD_ROW, plain);
        if (!exact.isEmpty()) {
            return new Lookup("headword", exact.stream().map(word -> card(word, null)).toList());
        }
        List<WordRow> byForm = jdbc.query(WORD_COLUMNS + """
                        where exists (select 1 from word_form f
                                      where f.word_id = w.id and f.form_plain = ?)
                        order by w.headword_plain, w.homonym_index
                        """,
                WORD_ROW, plain);
        if (byForm.isEmpty()) {
            return new Lookup("headword", List.of());
        }
        return new Lookup("form", byForm.stream().map(word -> card(word, matchedForm(word.id(), plain))).toList());
    }

    /**
     * Статья по идентификатору слова — переход, при котором гадать не надо.
     *
     * <p>Рядом идут омонимы того же написания: они часть той же статьи, а не другое место,
     * куда можно уйти по ссылке.
     */
    public Lookup lookupById(long id) {
        List<WordRow> word = jdbc.query(WORD_COLUMNS + "where w.id = ?", WORD_ROW, id);
        if (word.isEmpty()) {
            return new Lookup("id", List.of());
        }
        List<WordRow> homonyms = jdbc.query(
                WORD_COLUMNS + "where w.headword_plain = ? order by homonym_index",
                WORD_ROW, word.getFirst().headwordPlain());
        return new Lookup("id", homonyms.stream().map(row -> card(row, null)).toList());
    }

    /** Форма слова, совпавшая с написанием запроса, — чем объясняется попадание в список. */
    private Form matchedForm(long wordId, String plain) {
        List<Form> matched = jdbc.query("""
                        select grammar, coalesce(form, form_plain) as form from word_form
                        where word_id = ? and form_plain = ?
                          and coalesce(grammar, '') not like '%.lat'
                        order by grammar limit 1
                        """,
                (rs, i) -> new Form(rs.getString("grammar"), rs.getString("form")), wordId, plain);
        return matched.isEmpty() ? null : matched.getFirst();
    }

    private WordCard card(WordRow word, Form matchedForm) {
        List<SenseRow> senses = jdbc.query("""
                select id, number, ordinal from sense where word_id = ? order by ordinal
                """, (rs, i) -> new SenseRow(rs.getLong("id"),
                (Integer) rs.getObject("number")), word.id());

        Map<Long, List<String>> translations = translationsBySense(word.id());
        Map<Long, List<Example>> examplesBySense = examplesBySense(word.id());
        List<Example> idioms = idioms(word.id());
        // Грамматическая помета для показа — род. Ярлыки словоформ («nom.sg») сюда
        // не годятся: это внутренние обозначения указателя, а не пометы словаря.
        String gender = jdbc.query("select gender from word where id = ?",
                rs -> rs.next() ? rs.getString(1) : null, word.id());
        List<WordForm> forms = forms(word.id(), word.headword(), word.partOfSpeech(), gender);
        List<Root> roots = roots(word.id());

        List<Sense> senseCards = new ArrayList<>();
        for (SenseRow sense : senses) {
            senseCards.add(new Sense(sense.number(),
                    translations.getOrDefault(sense.id(), List.of()),
                    examplesBySense.getOrDefault(sense.id(), List.of())));
        }
        List<String> grammar = switch (gender == null ? "" : gender) {
            case "MASCULINE" -> List.of("м");
            case "FEMININE" -> List.of("ж");
            case "NEUTER" -> List.of("с");
            default -> List.<String>of();
        };

        return new WordCard(word.id(), word.headwordPlain(), word.headword(), word.headwordLatin(),
                grammar, word.partOfSpeech(), senseCards, idioms, forms, roots, word.status(),
                word.needsReview(), word.reviewReason(), word.homonymIndex(), matchedForm);
    }

    private List<FoundWord> attachTranslations(List<FoundWord> found) {
        List<FoundWord> result = new ArrayList<>(found.size());
        for (FoundWord word : found) {
            List<String> translations = jdbc.queryForList("""
                    select t.text from translation t
                    join sense s on s.id = t.sense_id
                    where s.word_id = ? order by s.ordinal, t.ordinal limit 6
                    """, String.class, word.id());
            result.add(word.withTranslations(translations));
        }
        return result;
    }

    private Map<Long, List<String>> translationsBySense(long wordId) {
        Map<Long, List<String>> bySense = new LinkedHashMap<>();
        jdbc.query("""
                select s.id as sense_id, t.text from translation t
                join sense s on s.id = t.sense_id
                where s.word_id = ? order by s.ordinal, t.ordinal
                """, rs -> {
            bySense.computeIfAbsent(rs.getLong("sense_id"), k -> new ArrayList<>())
                    .add(rs.getString("text"));
        }, wordId);
        return bySense;
    }

    private Map<Long, List<Example>> examplesBySense(long wordId) {
        Map<Long, List<Example>> bySense = new LinkedHashMap<>();
        jdbc.query("""
                select sense_id, serbian, russian from example
                where word_id = ? and not is_idiom and sense_id is not null
                order by ordinal
                """, rs -> {
            bySense.computeIfAbsent(rs.getLong("sense_id"), k -> new ArrayList<>())
                    .add(new Example(rs.getString("serbian"), rs.getString("russian")));
        }, wordId);
        return bySense;
    }

    private List<Example> idioms(long wordId) {
        return jdbc.query("""
                select serbian, russian from example
                where word_id = ? and is_idiom order by ordinal
                """, (rs, i) -> new Example(rs.getString("serbian"), rs.getString("russian")), wordId);
    }

    private List<WordForm> forms(long wordId, String headword, String partOfSpeech, String gender) {
        // Тип склонения — это и есть правило, по которому получены формы: по нему карточка
        // показывает полную парадигму с примерами и исключениями.
        String ruleType = "NOUN".equals(partOfSpeech) && gender != null
                ? NounDeclension.typeOf(headword, Gender.valueOf(gender)).name()
                : null;
        List<WordForm> stored = jdbc.query("""
                select coalesce(form, form_plain) as form, grammar, source from word_form
                where word_id = ? and coalesce(grammar, '') not like '%.lat'
                order by grammar, is_preferred desc, form_plain
                """, (rs, i) -> {
            String grammar = rs.getString("grammar");
            String source = rs.getString("source");
            boolean byRule = "RULES".equals(source) && "NOUN".equals(partOfSpeech);
            return new WordForm(rs.getString("form"), grammar, source,
                    byRule ? "noun-declension" : null, byRule ? ruleType : null);
        }, wordId);
        if (!"NOUN".equals(partOfSpeech) || gender == null) {
            return stored;
        }
        java.util.Set<String> known = new java.util.HashSet<>();
        for (WordForm form : stored) {
            known.add(form.grammar() + "\u0000" + Serbian.stripCombiningAccents(form.form()));
        }
        List<WordForm> result = new ArrayList<>(stored);
        for (Form form : NounDeclension.regularParadigm(headword, Gender.valueOf(gender))) {
            String key = form.grammar() + "\u0000" + form.value();
            if (known.add(key)) {
                result.add(new WordForm(form.value(), form.grammar(), "RULES", "noun-declension",
                        NounDeclension.typeOf(headword, Gender.valueOf(gender)).name()));
            }
        }
        return result;
    }

    // @tag:word-roots
    private List<Root> roots(long wordId) {
        return jdbc.query("""
                select kind, value, note from word_root where word_id = ? order by kind, value
                """, (rs, i) -> new Root(rs.getString("kind"), rs.getString("value"),
                rs.getString("note")), wordId);
    }

    private static final RowMapper<WordRow> WORD_ROW = (rs, i) -> new WordRow(
            rs.getLong("id"), rs.getString("headword"), rs.getString("headword_plain"),
            rs.getString("headword_latin"), rs.getString("part_of_speech"),
            rs.getString("status"), rs.getBoolean("needs_language_review"),
            rs.getString("review_reason"), rs.getInt("homonym_index"));

    // --- строки таблиц ---
    private record WordRow(long id, String headword, String headwordPlain, String headwordLatin,
                           String partOfSpeech, String status, boolean needsReview,
                           String reviewReason, int homonymIndex) {
    }

    private record SenseRow(long id, Integer number) {
    }

    // --- то, что отдаётся наружу ---

    /**
     * Найденное слово в списке поиска.
     *
     * @param matchedForm форма, по которой слово нашлось; {@code null}, когда совпало
     *                    само заглавное слово или перевод
     */
    public record FoundWord(long id, String headword, String headwordPlain, String headwordLatin,
                            String partOfSpeech, String status, List<String> translations,
                            Form matchedForm) {
        FoundWord withTranslations(List<String> translations) {
            return new FoundWord(id, headword, headwordPlain, headwordLatin, partOfSpeech,
                    status, translations, matchedForm);
        }

        FoundWord withMatchedForm(Form matchedForm) {
            return new FoundWord(id, headword, headwordPlain, headwordLatin, partOfSpeech,
                    status, translations, matchedForm);
        }
    }

    /**
     * Что нашлось по запросу статьи.
     *
     * @param matchedBy {@code headword} — совпало написание слова (тогда в списке омонимы
     *                  одного написания), {@code form} — совпала словоформа (тогда в списке
     *                  разные слова), {@code id} — переход по конкретному слову
     */
    public record Lookup(String matchedBy, List<WordCard> words) {
    }

    /** Карточка слова. */
    public record WordCard(long id, String name, String headword, String headwordLatin,
                           List<String> grammar, String partOfSpeech, List<Sense> senses,
                           List<Example> idioms, List<WordForm> forms, List<Root> roots,
                           String status, boolean needsLanguageReview, String reviewReason,
                           int homonymIndex, Form matchedForm) {
    }

    public record Sense(Integer number, List<String> translations, List<Example> examples) {
    }

    public record Example(String serbian, String russian) {
    }

    /**
     * Форма слова в карточке.
     *
     * @param source   откуда взята: {@code SOURCE_DICTIONARY}, {@code RULES}, …
     * @param rule     ключ правила, породившего форму; {@code null} — форма не от правила
     * @param ruleType разновидность правила (тип склонения) — по ней показывается
     *                 полная парадигма с примерами и исключениями
     */
    public record WordForm(String form, String grammar, String source, String rule, String ruleType) {
    }

    // @tag:word-roots
    public record Root(String kind, String value, String note) {
    }
}
