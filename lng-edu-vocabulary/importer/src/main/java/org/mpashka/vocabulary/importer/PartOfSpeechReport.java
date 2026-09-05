package org.mpashka.vocabulary.importer;

import org.mpashka.vocabulary.core.Chunk;
import org.mpashka.vocabulary.core.EntryParser;
import org.mpashka.vocabulary.core.MarkupParser;
import org.mpashka.vocabulary.core.PartOfSpeech;
import org.mpashka.vocabulary.core.PartOfSpeechRules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Отчёт о качестве правил определения части речи (этап 2 плана).
 *
 * <p>Проверка устроена как <b>сверка вслепую</b>: у статей, где часть речи названа явной
 * пометой, помету скрывают и смотрят, восстановят ли её структурные правила. Явная помета
 * служит образцом для сравнения, поэтому размечать что-либо руками не требуется —
 * образец даёт сама исходная база.
 *
 * <p>Запуск: {@code ./gradlew :importer:run}
 */
// @tag:part-of-speech @tag:import
public final class PartOfSpeechReport {

    /** Сколько примеров печатать для ручного просмотра. */
    private static final int SAMPLE_SIZE = 15;

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : SourceReader.DEFAULT_PATH;

        int total = 0;
        int withMark = 0;
        int recovered = 0;
        int structureSilent = 0;
        int structureWrong = 0;
        int noMarkTotal = 0;
        int noMarkResolved = 0;

        Map<PartOfSpeech, int[]> byExpected = new EnumMap<>(PartOfSpeech.class);
        Map<PartOfSpeech, Integer> finalCounts = new EnumMap<>(PartOfSpeech.class);
        List<String> wrongSamples = new ArrayList<>();
        List<String> unknownSamples = new ArrayList<>();
        List<String> adjectiveSamples = new ArrayList<>();

        SourceReader reader = new SourceReader(path);
        for (var row : collect(reader)) {
            total++;
            List<Chunk> chunks = MarkupParser.parse(row.xml());
            List<String> marks = EntryParser.collectMarks(chunks);

            PartOfSpeech expected = PartOfSpeechRules.detectByMark(marks);
            PartOfSpeech structural = PartOfSpeechRules.detectByStructure(chunks);
            PartOfSpeech actual = PartOfSpeechRules.detect(marks, chunks);
            finalCounts.merge(actual, 1, Integer::sum);

            if (expected != PartOfSpeech.UNKNOWN) {
                withMark++;
                int[] counters = byExpected.computeIfAbsent(expected, key -> new int[3]);
                counters[0]++;
                if (structural == expected) {
                    recovered++;
                    counters[1]++;
                } else if (structural == PartOfSpeech.UNKNOWN) {
                    structureSilent++;
                } else {
                    structureWrong++;
                    counters[2]++;
                    if (wrongSamples.size() < SAMPLE_SIZE) {
                        wrongSamples.add("  %-18s помета %-12s строение %-12s"
                                .formatted(row.name(), expected, structural));
                    }
                }
            } else {
                noMarkTotal++;
                if (structural != PartOfSpeech.UNKNOWN) {
                    noMarkResolved++;
                    // Правило прилагательного проверить пометами нельзя — помета «прил»
                    // стоит всего у 41 статьи. Поэтому откладываем образец на просмотр глазами.
                    if (structural == PartOfSpeech.ADJECTIVE && adjectiveSamples.size() < SAMPLE_SIZE
                            && noMarkTotal % 37 == 0) {
                        adjectiveSamples.add("  %-20s %s".formatted(row.name(), shorten(row.xml())));
                    }
                } else if (unknownSamples.size() < SAMPLE_SIZE) {
                    unknownSamples.add("  %-18s %s".formatted(row.name(), shorten(row.xml())));
                }
            }
        }

        System.out.println("=== Правила определения части речи ===");
        System.out.printf("Всего статей: %d%n%n", total);

        System.out.println("--- Покрытие ---");
        final int totalRows = total;
        finalCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> System.out.printf("  %-14s %6d  %5.1f%%%n",
                        e.getKey(), e.getValue(), percent(e.getValue(), totalRows)));
        int unknown = finalCounts.getOrDefault(PartOfSpeech.UNKNOWN, 0);
        System.out.printf("%nОпределено: %d из %d (%.1f%%)%n",
                total - unknown, total, percent(total - unknown, total));

        System.out.println("\n--- Сверка вслепую: восстанавливает ли строение скрытую помету ---");
        System.out.printf("Статей с явной пометой: %d%n", withMark);
        System.out.printf("  строение угадало верно:      %6d  (%.1f%%)%n",
                recovered, percent(recovered, withMark));
        System.out.printf("  строение промолчало:         %6d  (%.1f%%)%n",
                structureSilent, percent(structureSilent, withMark));
        System.out.printf("  строение ошиблось:           %6d  (%.1f%%)  <- цена правил%n",
                structureWrong, percent(structureWrong, withMark));
        System.out.printf("%nТочность строения там, где оно высказалось: %.1f%%%n",
                percent(recovered, recovered + structureWrong));

        System.out.println("\n--- По частям речи (образец — явная помета) ---");
        System.out.printf("  %-14s %8s %8s %8s%n", "часть речи", "всего", "верно", "ошибок");
        byExpected.forEach((pos, counters) -> System.out.printf("  %-14s %8d %8d %8d%n",
                pos, counters[0], counters[1], counters[2]));

        System.out.println("\n--- Статьи без явной пометы ---");
        System.out.printf("Всего: %d, из них строение определило: %d (%.1f%%)%n",
                noMarkTotal, noMarkResolved, percent(noMarkResolved, noMarkTotal));

        if (!wrongSamples.isEmpty()) {
            System.out.println("\n--- Примеры ошибок строения ---");
            wrongSamples.forEach(System.out::println);
        }
        System.out.println("\n--- Прилагательные без пометы: образец на просмотр глазами ---");
        adjectiveSamples.forEach(System.out::println);

        System.out.println("\n--- Примеры неопределённых (пойдут в очередь на LLM) ---");
        unknownSamples.forEach(System.out::println);
    }

    private static List<SourceReader.Row> collect(SourceReader reader) {
        List<SourceReader.Row> rows = new ArrayList<>(46_000);
        reader.forEach(rows::add);
        return rows;
    }

    private static double percent(int part, int whole) {
        return whole == 0 ? 0 : 100.0 * part / whole;
    }

    private static String shorten(String markup) {
        String value = markup.length() > 70 ? markup.substring(0, 70) + "…" : markup;
        return value.replace("\n", " ");
    }

    private PartOfSpeechReport() {
    }
}
