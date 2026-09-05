package org.mpashka.vocabulary.core;

/**
 * Русский текст в исходной базе.
 *
 * <p>Ударение в русских словах записано знаком U+2019 <b>после</b> ударной гласной:
 * {@code вода’}. Это тот же самый символ, которым в сербском обозначено долгое
 * восходящее ударение, — различить их можно только по помете фрагмента
 * ({@code C}/{@code CL} — сербский, {@code RV} и пустая помета — русский).
 *
 * <p>Поэтому преобразование русского ударения живёт отдельно от {@link Serbian}
 * и применяется только к тому тексту, про который разбор уже знает, что он русский.
 */
// @tag:accent
public final class Russian {

    /** Знак ударения в исходной базе. */
    private static final char SOURCE_MARK = '’';

    /** Комбинируемый знак ударения Unicode. */
    private static final char COMBINING_ACUTE = '́';

    private Russian() {
    }

    /**
     * Заменяет апостроф после ударной гласной на комбинируемый знак ударения,
     * чтобы ударение показывалось над буквой: {@code вода’} → {@code вода́}.
     */
    public static String renderStress(String text) {
        if (text == null || text.indexOf(SOURCE_MARK) < 0) {
            return text;
        }
        return text.replace(SOURCE_MARK, COMBINING_ACUTE);
    }
}
