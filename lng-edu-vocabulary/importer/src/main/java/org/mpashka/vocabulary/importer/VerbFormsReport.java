package org.mpashka.vocabulary.importer;

import org.mpashka.vocabulary.core.Chunk;
import org.mpashka.vocabulary.core.MarkupParser;
import org.mpashka.vocabulary.core.PartOfSpeech;
import org.mpashka.vocabulary.core.PartOfSpeechRules;
import org.mpashka.vocabulary.core.Serbian;
import org.mpashka.vocabulary.core.VerbConjugation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Отчёт о качестве правил спряжения глаголов (этап 3 плана).
 *
 * <p>Образец даёт исходная база: после инфинитива она указывает первое лицо единственного
 * числа настоящего времени — целиком ({@code ра’дити, ра^ди_м}) либо сокращённо, с дефисом
 * ({@code аванзова‛ти, -зу’јем}). Правила порождают эту форму и сверяются с указанной.
 *
 * <p>Запуск: {@code ./gradlew :importer:runVerbForms}
 */
// @tag:word-forms @tag:import
public final class VerbFormsReport {

    private static final int SAMPLE_SIZE = 14;

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : SourceReader.DEFAULT_PATH;

        int verbs = 0;
        int withPresent = 0;
        int correct = 0;
        int viaCandidate = 0;
        int wrong = 0;
        long candidateTotal = 0;

        Map<String, int[]> byEnding = new HashMap<>();
        List<String> wrongSamples = new ArrayList<>();

        for (var row : rows(path)) {
            List<Chunk> chunks = MarkupParser.parse(row.xml());
            if (chunks.isEmpty()) {
                continue;
            }
            if (PartOfSpeechRules.detectByStructure(chunks) != PartOfSpeech.VERB) {
                continue;
            }
            verbs++;

            String infinitive = chunks.getFirst().text();
            String expected = presentFromSource(chunks, infinitive);
            if (expected == null) {
                continue;
            }
            withPresent++;

            String ending = endingOf(bare(infinitive));
            int[] counters = byEnding.computeIfAbsent(ending, key -> new int[2]);
            counters[0]++;

            String predicted = VerbConjugation.presentFirstSingular(infinitive);
            java.util.List<String> candidates = VerbConjugation.presentCandidates(infinitive);
            candidateTotal += candidates.size();

            expected = withoutReflexive(expected);
            predicted = withoutReflexive(predicted);
            candidates = candidates.stream().map(VerbFormsReport::withoutReflexive).toList();
            if (expected.equals(predicted)) {
                correct++;
                counters[1]++;
            } else if (candidates.contains(expected)) {
                viaCandidate++;
                counters[1]++;
            } else {
                wrong++;
                if (wrongSamples.size() < SAMPLE_SIZE) {
                    wrongSamples.add("  %-22s ждали %-18s вышло %s"
                            .formatted(bare(infinitive), expected, predicted));
                }
            }
        }

        int checked = correct + viaCandidate + wrong;
        System.out.println("=== Спряжение глаголов: 1 л. ед. ч. настоящего времени ===");
        System.out.printf("Глаголов (инфинитив на -ти/-ћи): %d%n", verbs);
        System.out.printf("Из них указана форма наст. вр.:  %d%n%n", withPresent);

        System.out.printf("  верно основным правилом: %6d  (%.1f%%)%n",
                correct, percent(correct, checked));
        System.out.printf("  верно доп. вариантом:    %6d  (%.1f%%)%n",
                viaCandidate, percent(viaCandidate, checked));
        System.out.printf("  ошиблись:                %6d  (%.1f%%)%n",
                wrong, percent(wrong, checked));
        System.out.printf("%nИтоговая точность: %.1f%%%n", percent(correct + viaCandidate, checked));
        System.out.printf("В среднем вариантов на слово: %.2f%n",
                checked == 0 ? 0 : (double) candidateTotal / checked);

        System.out.println("\n--- По окончанию инфинитива ---");
        System.out.printf("  %-10s %8s %8s %8s%n", "окончание", "всего", "верно", "точность");
        byEnding.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .limit(12)
                .forEach(e -> System.out.printf("  -%-9s %8d %8d %7.1f%%%n",
                        e.getKey(), e.getValue()[0], e.getValue()[1],
                        percent(e.getValue()[1], e.getValue()[0])));

        System.out.println("\n--- Примеры ошибок ---");
        wrongSamples.forEach(System.out::println);
    }

    /**
     * Форма настоящего времени, указанная в статье. Сокращённая запись (перед формой
     * стоит отдельный фрагмент «-») восстанавливается по инфинитиву.
     */
    private static String presentFromSource(List<Chunk> chunks, String infinitive) {
        boolean abbreviated = false;
        for (int i = 1; i < chunks.size() && i < 5; i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.isTranslation() || Chunk.SENSE_NUMBER.equals(chunk.tag())) {
                return null;
            }
            if (!chunk.hasTag() && chunk.text().startsWith("-")) {
                abbreviated = true;
                continue;
            }
            if (!Chunk.SERBIAN.equals(chunk.tag()) && !Chunk.SERBIAN_LINK.equals(chunk.tag())) {
                continue;
            }
            String form = bare(chunk.text());
            // Возвратная частица идёт отдельным фрагментом — это не форма, смотрим дальше.
            if (form.equals("се")) {
                continue;
            }
            // Тильда означает повтор основы — это не форма настоящего времени,
            // а статья другого устройства (причастие, устойчивое сочетание).
            if (form.isEmpty() || form.startsWith("~") || form.equals(bare(infinitive))) {
                return null;
            }
            // Дефис бывает отдельным фрагментом, внутри текста формы, а иногда
            // отсутствует вовсе. Тогда сокращение видно по самой форме: она короче
            // инфинитива и начинается с других букв («биберити, рим»).
            boolean shortened = abbreviated || form.startsWith("-")
                    || (form.length() < bare(infinitive).length()
                        && form.length() >= 2
                        && !bare(infinitive).startsWith(form.substring(0, 2)));
            return shortened ? VerbConjugation.expandAbbreviated(infinitive, form) : form;
        }
        return null;
    }

    /**
     * Отбрасывает возвратную частицу: словарь указывает её то при инфинитиве,
     * то при форме настоящего времени, то при обоих. Для сверки самой формы она лишняя.
     */
    private static String withoutReflexive(String form) {
        if (form == null) {
            return null;
        }
        String value = form.trim();
        return value.endsWith(" се") ? value.substring(0, value.length() - 3).trim() : value;
    }

    private static String endingOf(String bare) {
        for (String ending : new String[]{"овати", "ирати", "исати", "нути", "ивати",
                "евати", "јети", "ети", "ити", "ати", "ути", "сти", "ћи"}) {
            if (bare.endsWith(ending)) {
                return ending;
            }
        }
        return "прочее";
    }

    private static String bare(String text) {
        return Serbian.stripAccents(Serbian.stripStemMarker(text)).trim();
    }

    private static List<SourceReader.Row> rows(String path) {
        List<SourceReader.Row> rows = new ArrayList<>(46_000);
        new SourceReader(path).forEach(rows::add);
        return rows;
    }

    private static double percent(int part, int whole) {
        return whole == 0 ? 0 : 100.0 * part / whole;
    }

    private VerbFormsReport() {
    }
}
