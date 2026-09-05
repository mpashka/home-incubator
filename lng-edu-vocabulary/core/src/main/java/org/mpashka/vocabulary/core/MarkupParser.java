package org.mpashka.vocabulary.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Разбирает поле {@code words.xml} исходной базы на список {@link Chunk}.
 *
 * <p>Формат: поток фрагментов, разделённых {@code #}. Если во фрагменте есть {@code $},
 * то всё после <b>последнего</b> {@code $} — помета класса; помета может быть пустой,
 * и пустая помета значима. Если {@code $} нет вовсе, у фрагмента нет пометы.
 *
 * <p>Приставка {@code M} у пометы ({@code MC}, {@code MCL}, {@code MRV}, {@code M}) —
 * не смысловая помета, а подсказка отрисовки: она стоит тогда, когда во фрагменте есть
 * примыкающий небуквенный символ. Здесь она снимается, и фрагмент трактуется как
 * соответствующий основной класс.
 */
// @tag:markup
public final class MarkupParser {

    private MarkupParser() {
    }

    /** Разбирает статью на фрагменты. Приставка {@code M} снимается. */
    public static List<Chunk> parse(String markup) {
        List<Chunk> chunks = new ArrayList<>();
        if (markup == null || markup.isEmpty()) {
            return chunks;
        }
        for (String part : markup.split("#", -1)) {
            if (part.isEmpty()) {
                continue;
            }
            int marker = part.lastIndexOf('$');
            if (marker < 0) {
                chunks.add(new Chunk(part, null));
            } else {
                chunks.add(new Chunk(part.substring(0, marker), normalizeTag(part.substring(marker + 1))));
            }
        }
        return chunks;
    }

    /**
     * Снимает приставку {@code M}: {@code MC} → {@code C}, {@code MCL} → {@code CL},
     * {@code MRV} → {@code RV}, {@code M} → пустая помета.
     *
     * <p>Помета {@code N}/{@code NC} — опечатки исходных данных (16 фрагментов на всю
     * базу), трактуется как текст без класса.
     *
     * <p>Осторожно: {@code DC} и {@code DRV} — это <b>не</b> номера значений, а обычные
     * числа внутри фразы ({@code има 24 часа}, {@code моне’та в 2 дина’ра}). Сводить их
     * к {@link Chunk#SENSE_NUMBER} нельзя — появятся призрачные значения.
     */
    private static String normalizeTag(String tag) {
        return switch (tag) {
            case "MC" -> Chunk.SERBIAN;
            case "MCL" -> Chunk.SERBIAN_LINK;
            case "MRV" -> Chunk.TRANSLATION;
            case "M" -> Chunk.RUSSIAN_PLAIN;
            case "SC" -> Chunk.SYMBOL;
            case "DC" -> Chunk.SERBIAN;
            case "DRV" -> Chunk.TRANSLATION;
            case "N", "NC" -> Chunk.RUSSIAN_PLAIN;
            default -> tag;
        };
    }
}
