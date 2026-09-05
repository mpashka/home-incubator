package org.mpashka.vocabulary.core;

/**
 * Фрагмент разметки исходной статьи: кусок текста со своей пометой класса.
 *
 * <p>Поле {@code words.xml} исходной базы, вопреки названию, не XML, а плоский поток
 * фрагментов вида {@code текст$ТЕГ#}. Разбирается {@link MarkupParser}.
 *
 * @param text текст фрагмента
 * @param tag  помета класса: {@code null} — во фрагменте не было {@code $}
 *             (обычно знак препинания); пустая строка — пустая помета, она значима
 *             и означает «прочий русский текст»; иначе {@code C}, {@code CL},
 *             {@code RV}, {@code D}, {@code S}, {@code PC}, {@code PRV}
 */
// @tag:markup
public record Chunk(String text, String tag) {

    /** Сербский текст. */
    public static final String SERBIAN = "C";

    /** Сербский текст, являющийся ссылкой на другую статью. */
    public static final String SERBIAN_LINK = "CL";

    /** Русский перевод заглавного слова. */
    public static final String TRANSLATION = "RV";

    /** Номер значения. */
    public static final String SENSE_NUMBER = "D";

    /** Особый символ: {@code ◊} — начало фразеологии, {@code ≈} — приблизительное соответствие. */
    public static final String SYMBOL = "S";

    /** Знак препинания внутри сербской части. */
    public static final String SERBIAN_PUNCTUATION = "PC";

    /** Знак препинания внутри русской части. */
    public static final String RUSSIAN_PUNCTUATION = "PRV";

    /** Прочий русский текст: пометы, перевод примеров, римские цифры омонимов. */
    public static final String RUSSIAN_PLAIN = "";

    /** Есть ли у фрагмента помета вообще. */
    public boolean hasTag() {
        return tag != null;
    }

    /** Относится ли фрагмент к сербской части статьи. */
    public boolean isSerbian() {
        return SERBIAN.equals(tag) || SERBIAN_LINK.equals(tag) || SERBIAN_PUNCTUATION.equals(tag);
    }

    /** Ссылка ли это на другую статью словаря. */
    public boolean isLink() {
        return SERBIAN_LINK.equals(tag);
    }

    /** Перевод ли это заглавного слова. */
    public boolean isTranslation() {
        return TRANSLATION.equals(tag);
    }

    /**
     * Прочий русский текст — пустая помета. Именно так размечен перевод примеров
     * и фразеология, поэтому отличать её от {@link #isTranslation()} принципиально.
     */
    public boolean isRussianPlain() {
        return RUSSIAN_PLAIN.equals(tag);
    }

    /**
     * Знак препинания, разделяющий переводы и примеры.
     *
     * <p>Разделитель приходит в двух видах: без пометы вовсе и с пометой {@code PRV}.
     * Оба надо разбирать одинаково, иначе запятая внутри перевода теряется
     * ({@code портно’й$RV#,$PRV#шью’щий$RV#}).
     */
    public boolean isSeparator() {
        return !hasTag() || RUSSIAN_PUNCTUATION.equals(tag);
    }
}
