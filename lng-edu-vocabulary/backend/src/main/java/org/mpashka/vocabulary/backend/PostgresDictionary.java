package org.mpashka.vocabulary.backend;

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
     * Поиск слов. Ищет по началу заглавного слова (латиница и кириллица),
     * <b>по любой словоформе</b> и по русскому переводу.
     */
    public List<FoundWord> search(String query, int limit, String alphabet) {
        String normalized = query.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return List.of();
        }
        String prefix = normalized + "%";
        boolean cyrillic = normalized.codePoints()
                .anyMatch(codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.CYRILLIC);
        String serbian = """
                w.headword_plain like ?
                   or lower(w.headword_latin) like ?
                   or exists (select 1 from word_form f
                              where f.word_id = w.id and (f.form_plain like ?
                                or lower(translate(replace(replace(replace(f.form_plain,
                                    'љ', 'lj'), 'њ', 'nj'), 'џ', 'dž'),
                                    'абвгдђежзијклмнопрстћуфхцчш',
                                    'abvgdđežzijklmnoprstćufhcčš')) like ?))
                """;
        String russian = """
                exists (select 1 from translation t
                        join sense s on s.id = t.sense_id
                        where s.word_id = w.id and lower(t.text) like ?)
                """;
        String where = switch (alphabet) {
            case "separate" -> cyrillic ? russian : serbian;
            case "any" -> "(" + serbian + " or " + russian + ")";
            default -> cyrillic ? "(" + serbian + " or " + russian + ")" : serbian;
        };
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
        if (!"separate".equals(alphabet) || !cyrillic) {
            arguments.add(prefix);
            arguments.add(prefix);
            arguments.add(prefix);
            arguments.add(prefix);
        }
        if (!"separate".equals(alphabet) || cyrillic) {
            arguments.add("%" + normalized + "%");
        }
        arguments.add(prefix);
        arguments.add(limit);
        List<FoundWord> found = jdbc.query(sql,
                (rs, i) -> new FoundWord(rs.getLong("id"), rs.getString("headword"),
                        rs.getString("headword_plain"), rs.getString("headword_latin"),
                        rs.getString("part_of_speech"), rs.getString("status"), List.of()),
                arguments.toArray());

        // Переводы одним запросом для всех найденных слов.
        return attachTranslations(found);
    }

    /**
     * Статьи по заглавному слову без ударений.
     *
     * <p>Возвращает <b>список</b>: под одним написанием может лежать несколько омонимов
     * ({@code бити} — «быть» и «бить»), и показывать только первый значило бы прятать
     * половину словаря.
     */
    public List<WordCard> byHeadword(String headwordPlain) {
        String name = Serbian.stripCombiningAccents(headwordPlain).toLowerCase();
        List<WordRow> words = jdbc.query("""
                        select id, headword, headword_plain, headword_latin,
                               part_of_speech, status, needs_language_review, review_reason,
                               homonym_index
                        from word w where w.headword_plain = ?
                           or exists (select 1 from word_form f
                                      where f.word_id = w.id and f.form_plain = ?)
                        order by homonym_index
                        """,
                WORD_ROW, name, name);
        return words.stream().map(this::card).toList();
    }

    private WordCard card(WordRow word) {
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

        return new WordCard(word.headwordPlain(), word.headword(), word.headwordLatin(),
                grammar, word.partOfSpeech(), senseCards, idioms, forms, roots, word.status(),
                word.needsReview(), word.reviewReason(), word.homonymIndex());
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
        List<WordForm> stored = jdbc.query("""
                select coalesce(form, form_plain) as form, grammar, source from word_form
                where word_id = ? and coalesce(grammar, '') not like '%.lat'
                order by grammar, is_preferred desc, form_plain
                """, (rs, i) -> {
            String grammar = rs.getString("grammar");
            if (grammar == null && "NOUN".equals(partOfSpeech)) {
                grammar = "gen.sg";
            }
            String source = rs.getString("source");
            return new WordForm(rs.getString("form"), grammar, source,
                    "RULES".equals(source) && "NOUN".equals(partOfSpeech) ? "noun-declension" : null);
        }, wordId);
        if (!"NOUN".equals(partOfSpeech) || gender == null) {
            return stored;
        }
        java.util.Set<String> known = new java.util.HashSet<>();
        for (WordForm form : stored) {
            known.add(form.grammar() + "\u0000" + Serbian.stripCombiningAccents(form.form()));
        }
        List<WordForm> result = new ArrayList<>(stored);
        for (NounDeclension.Form form : NounDeclension.regularParadigm(headword, Gender.valueOf(gender))) {
            String key = form.grammar() + "\u0000" + form.value();
            if (known.add(key)) {
                result.add(new WordForm(form.value(), form.grammar(), "RULES", "noun-declension"));
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

    /** Найденное слово в списке поиска. */
    public record FoundWord(long id, String headword, String headwordPlain, String headwordLatin,
                            String partOfSpeech, String status, List<String> translations) {
        FoundWord withTranslations(List<String> translations) {
            return new FoundWord(id, headword, headwordPlain, headwordLatin, partOfSpeech,
                    status, translations);
        }
    }

    /** Карточка слова. */
    public record WordCard(String name, String headword, String headwordLatin, List<String> grammar,
                           String partOfSpeech, List<Sense> senses, List<Example> idioms,
                           List<WordForm> forms, List<Root> roots, String status,
                           boolean needsLanguageReview, String reviewReason, int homonymIndex) {
    }

    public record Sense(Integer number, List<String> translations, List<Example> examples) {
    }

    public record Example(String serbian, String russian) {
    }

    public record WordForm(String form, String grammar, String source, String rule) {
    }

    // @tag:word-roots
    public record Root(String kind, String value, String note) {
    }
}
