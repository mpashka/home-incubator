package org.mpashka.vocabulary.core;

/**
 * Сербское ударение. В сербском языке четыре тона плюс заударная долгота.
 *
 * <p>В исходной базе тон закодирован обычным знаком препинания, который стоит
 * <b>после</b> гласной. Соответствие знаков установлено обратной разработкой и
 * проверено дважды — по словам с известной нормой и структурно (нисходящие тоны
 * встречаются почти только на первом слоге). Подробности — в
 * {@code docs/implementation/source-db.md}, раздел 3.
 *
 * @see <a href="../../../../../../../docs/implementation/source-db.md">docs/implementation/source-db.md</a>
 */
// @tag:accent
public enum Accent {

    /** Краткое восходящее, {@code ù}. Знак в исходной базе — U+201B. */
    SHORT_RISING('‛', '̀', "краткое восходящее"),

    /** Долгое восходящее, {@code ú}. Знак в исходной базе — U+2019. */
    LONG_RISING('’', '́', "долгое восходящее"),

    /** Краткое нисходящее, {@code ȕ}. Знак в исходной базе — U+201C. */
    SHORT_FALLING('“', '̏', "краткое нисходящее"),

    /** Долгое нисходящее, {@code ȗ}. Знак в исходной базе — U+005E. */
    LONG_FALLING('^', '̑', "долгое нисходящее"),

    /** Заударная долгота, {@code ū}. Знак в исходной базе — U+005F. Не тон. */
    LENGTH('_', '̄', "заударная долгота");

    private final char sourceMark;
    private final char combining;
    private final String title;

    Accent(char sourceMark, char combining, String title) {
        this.sourceMark = sourceMark;
        this.combining = combining;
        this.title = title;
    }

    /** Знак, которым ударение записано в исходной базе. */
    public char sourceMark() {
        return sourceMark;
    }

    /** Комбинируемый знак Unicode для показа над гласной. */
    public char combining() {
        return combining;
    }

    /** Название по-русски. */
    public String title() {
        return title;
    }

    /** Тон ли это (в отличие от заударной долготы). */
    public boolean isTone() {
        return this != LENGTH;
    }

    /** Ударение по знаку из исходной базы либо {@code null}, если знак не ударение. */
    public static Accent bySourceMark(char mark) {
        for (Accent accent : values()) {
            if (accent.sourceMark == mark) {
                return accent;
            }
        }
        return null;
    }
}
