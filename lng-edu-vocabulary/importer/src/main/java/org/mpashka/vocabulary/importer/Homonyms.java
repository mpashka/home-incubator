package org.mpashka.vocabulary.importer;

import org.mpashka.vocabulary.core.Chunk;
import org.mpashka.vocabulary.core.MarkupParser;
import org.mpashka.vocabulary.core.Serbian;

import java.util.ArrayList;
import java.util.List;

/**
 * Разделение омонимов, упакованных исходной базой в одну строку.
 *
 * <p>Ключ исходной таблицы — заглавное слово, поэтому несколько самостоятельных слов
 * лежат в одной записи, разделённые римскими цифрами:
 *
 * <pre>
 *   би“ти$C#I$#,#бу“де_м$C#(#је‛сам$C#)#быть$RV#…
 *   би“ти$C#II$#,#би“је_м$C#1$D#)#бить$RV#…
 * </pre>
 *
 * <p>Разделение важно не только ради отдельных карточек: <b>у омонимов бывает разное
 * ударение</b>, и поле {@code words.stress} хранит только ударение первого. Проверено
 * на всей базе: из 1 269 статей-омонимов ударение различается у <b>711</b>
 * ({@code а‛ла_т} / {@code а“лат}, {@code ба“ба} / {@code ба’ба}). В разметке оба
 * ударения есть — значит, восстанавливаются разбором, а не внешними источниками.
 */
// @tag:import @tag:accent
public final class Homonyms {

    private Homonyms() {
    }

    /**
     * Делит разметку статьи на части — по одной на омоним.
     *
     * <p>Границей служит <b>повтор заглавного слова</b>, за которым в шапке идёт римская
     * цифра. Между повтором и цифрой могут стоять грамматические пометы
     * ({@code го‛ра$C#ж$#.#,#го“ра$C#ж$#.#I$#}), поэтому цифру ищем до первого перевода
     * или номера значения.
     *
     * @return части по числу омонимов; один элемент, если омонимов нет
     */
    public static List<Homonym> split(String markup) {
        List<Chunk> chunks = MarkupParser.parse(markup);
        if (chunks.isEmpty()) {
            return List.of(new Homonym(markup, null));
        }
        String headword = plain(chunks.getFirst().text());

        List<Integer> starts = new ArrayList<>();
        List<String> numerals = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (!Chunk.SERBIAN.equals(chunk.tag()) || !plain(chunk.text()).equals(headword)) {
                continue;
            }
            String numeral = numeralAfter(chunks, i);
            if (numeral == null) {
                continue;
            }
            // Заглавное слово бывает выписано несколькими вариантами ударения подряд
            // (го‛ра ж., го“ра ж. I) — цифра стоит только за последним. Началом омонима
            // считаем первый вариант группы, иначе первый вариант ударения потеряется.
            int start = groupStart(chunks, i, headword);
            if (!starts.isEmpty() && start <= starts.getLast()) {
                continue;
            }
            starts.add(start);
            numerals.add(numeral);
        }

        if (starts.size() < 2) {
            return List.of(new Homonym(markup, null));
        }

        List<Homonym> parts = new ArrayList<>(starts.size());
        for (int b = 0; b < starts.size(); b++) {
            // Первый омоним забирает и всё, что стоит до него, — иначе начало статьи
            // потерялось бы.
            int from = b == 0 ? 0 : starts.get(b);
            int to = b + 1 < starts.size() ? starts.get(b + 1) : chunks.size();
            parts.add(new Homonym(rebuild(chunks.subList(from, to)), numerals.get(b)));
        }
        return parts;
    }

    /**
     * Начало группы подряд идущих повторов заглавного слова: отматывает назад через
     * пометы и знаки препинания, пока встречаются повторы.
     */
    private static int groupStart(List<Chunk> chunks, int index, String headword) {
        int start = index;
        for (int j = index - 1; j >= 0; j--) {
            Chunk chunk = chunks.get(j);
            if (Chunk.SERBIAN.equals(chunk.tag()) && plain(chunk.text()).equals(headword)) {
                start = j;
                continue;
            }
            // Через пометы и пунктуацию перешагиваем, через содержание статьи — нет.
            boolean skippable = !chunk.hasTag()
                    || (Chunk.RUSSIAN_PLAIN.equals(chunk.tag()) && chunk.text().length() <= 8);
            if (!skippable) {
                break;
            }
        }
        return start;
    }

    /** Римская цифра в шапке после повтора заглавного слова либо {@code null}. */
    private static String numeralAfter(List<Chunk> chunks, int index) {
        for (int j = index + 1; j < chunks.size() && j < index + 7; j++) {
            Chunk next = chunks.get(j);
            if (next.isTranslation() || Chunk.SENSE_NUMBER.equals(next.tag())) {
                return null;
            }
            if (Chunk.RUSSIAN_PLAIN.equals(next.tag()) && isRomanNumeral(next.text().trim())) {
                return next.text().trim();
            }
        }
        return null;
    }

    /** Собирает разметку обратно из фрагментов — в точности как она была записана. */
    private static String rebuild(List<Chunk> chunks) {
        StringBuilder markup = new StringBuilder();
        for (Chunk chunk : chunks) {
            markup.append(chunk.text());
            if (chunk.hasTag()) {
                markup.append('$').append(chunk.tag());
            }
            markup.append('#');
        }
        return markup.toString();
    }

    private static String plain(String text) {
        return Serbian.stripAccents(Serbian.stripStemMarker(text)).trim();
    }

    private static boolean isRomanNumeral(String text) {
        return text.matches("I{1,3}|IV|VI{0,3}");
    }

    /**
     * Один омоним.
     *
     * @param markup  разметка только этого омонима
     * @param numeral римская цифра из словаря ({@code I}, {@code II}); {@code null},
     *                если омонимов в статье нет
     */
    public record Homonym(String markup, String numeral) {
    }
}
