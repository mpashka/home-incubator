package org.mpashka.vocabulary.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сербское письмо: перевод кириллицы в латиницу и показ ударений.
 *
 * <p>Каноническим видом слова в проекте считается <b>кириллица со знаками ударения</b>,
 * а латиница из неё выводится. Направление кириллица → латиница однозначно и проверено
 * на всех 45 633 записях исходной базы. Обратное направление неоднозначно
 * ({@code nj} — это и {@code њ}, и {@code нј}), поэтому латиница источником быть не может.
 */
// @tag:accent
public final class Serbian {

    private static final Map<Character, String> CYRILLIC_TO_LATIN = new LinkedHashMap<>();

    static {
        String cyrillic = "абвгдђежзијклљмнњопрстћуфхцчџш";
        String[] latin = {"a", "b", "v", "g", "d", "đ", "e", "ž", "z", "i", "j", "k", "l",
                "lj", "m", "n", "nj", "o", "p", "r", "s", "t", "ć", "u", "f", "h", "c",
                "č", "dž", "š"};
        for (int i = 0; i < cyrillic.length(); i++) {
            char lower = cyrillic.charAt(i);
            CYRILLIC_TO_LATIN.put(lower, latin[i]);
            CYRILLIC_TO_LATIN.put(Character.toUpperCase(lower), capitalize(latin[i]));
        }
    }

    private Serbian() {
    }

    /**
     * Переводит сербскую кириллицу в латиницу. Знаки ударения и прочие символы
     * проходят насквозь без изменений.
     */
    public static String toLatin(String cyrillic) {
        StringBuilder result = new StringBuilder(cyrillic.length() + 4);
        for (int i = 0; i < cyrillic.length(); i++) {
            char ch = cyrillic.charAt(i);
            String replacement = CYRILLIC_TO_LATIN.get(ch);
            result.append(replacement != null ? replacement : ch);
        }
        return result.toString();
    }

    /**
     * Заменяет знаки ударения исходной базы на комбинируемые знаки Unicode, чтобы
     * ударение показывалось <b>над</b> гласной, а не отдельным символом после неё.
     *
     * <p>{@code во‛да} → {@code во̀да}
     */
    public static String renderAccents(String withSourceMarks) {
        StringBuilder result = new StringBuilder(withSourceMarks.length());
        for (int i = 0; i < withSourceMarks.length(); i++) {
            char ch = withSourceMarks.charAt(i);
            Accent accent = Accent.bySourceMark(ch);
            result.append(accent != null ? accent.combining() : ch);
        }
        return result.toString();
    }

    /** Убирает знаки ударения исходной базы, оставляя голое слово. */
    public static String stripAccents(String withSourceMarks) {
        StringBuilder result = new StringBuilder(withSourceMarks.length());
        for (int i = 0; i < withSourceMarks.length(); i++) {
            char ch = withSourceMarks.charAt(i);
            if (Accent.bySourceMark(ch) == null) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * Убирает комбинируемые знаки ударения (те, что ставит {@link #renderAccents}),
     * оставляя голое слово.
     *
     * <p>Нужно для уже отрисованного слова: {@code stripAccents} снимает знаки
     * <b>исходной базы</b> ({@code ‛’“^}), а после {@code renderAccents} в слове стоят
     * <b>комбинируемые</b> знаки Unicode ({@code во̀да}) — их снимает эта.
     */
    public static String stripCombiningAccents(String rendered) {
        StringBuilder result = new StringBuilder(rendered.length());
        for (int i = 0; i < rendered.length(); i++) {
            char ch = rendered.charAt(i);
            // Диапазон комбинируемых диакритических знаков Unicode: U+0300..U+036F.
            if (ch < '\u0300' || ch > '\u036F') {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * Убирает служебный знак {@code ||}, которым в заглавном слове отделена основа
     * от окончания.
     */
    public static String stripStemMarker(String headword) {
        return headword.replace("||", "");
    }

    /**
     * Основа заглавного слова — часть до {@code ||}. Если {@code ||} нет, основой
     * считается всё слово: тильда в примерах тогда заменяет слово целиком.
     */
    public static String stem(String headword) {
        int marker = headword.indexOf("||");
        return marker >= 0 ? headword.substring(0, marker) : headword;
    }

    /**
     * Раскрывает тильду — сокращение, которым в примерах заменяют основу заглавного слова.
     *
     * <p>Для статьи {@code во‛д||а} фрагмент {@code ~а} превращается в {@code во‛да}.
     */
    public static String expandTilde(String text, String headword) {
        return text.indexOf('~') < 0 ? text : text.replace("~", stem(headword));
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
