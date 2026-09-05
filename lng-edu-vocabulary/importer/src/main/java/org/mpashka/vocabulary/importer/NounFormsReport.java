package org.mpashka.vocabulary.importer;

import org.mpashka.vocabulary.core.Chunk;
import org.mpashka.vocabulary.core.EntryParser;
import org.mpashka.vocabulary.core.Gender;
import org.mpashka.vocabulary.core.MarkupParser;
import org.mpashka.vocabulary.core.NounDeclension;
import org.mpashka.vocabulary.core.Serbian;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Отчёт о качестве правил склонения существительных (этап 3 плана).
 *
 * <p>Образец для сверки даёт сама база: у существительных сразу после заглавного слова
 * указан <b>родительный падеж единственного числа</b> — либо целиком
 * ({@code аба‛жу_р, абажу’ра}), либо через тильду ({@code бе‛збедн||о_ст, ~ости}).
 * Правила порождают эту форму, и она сверяется с указанной.
 *
 * <p>Запуск: {@code ./gradlew :importer:runNounForms}
 */
// @tag:word-forms @tag:import
public final class NounFormsReport {

    private static final int SAMPLE_SIZE = 12;

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : SourceReader.DEFAULT_PATH;

        int nouns = 0;
        int withGenitive = 0;
        int correct = 0;
        int correctViaExtended = 0;
        long candidateTotal = 0;
        int pluralOnly = 0;
        int wrong = 0;

        Map<NounDeclension.Type, int[]> byType = new EnumMap<>(NounDeclension.Type.class);
        List<String> wrongSamples = new ArrayList<>();

        for (var row : rows(path)) {
            List<Chunk> chunks = MarkupParser.parse(row.xml());
            if (chunks.isEmpty()) {
                continue;
            }
            List<String> marks = EntryParser.collectMarks(chunks);
            Gender gender = Gender.fromMarks(marks);
            if (gender == null) {
                continue;
            }
            // Слова только во множественном числе (би̏саге, Мле̑ци) склоняются иначе,
            // и вторым фрагментом у них стоит родительный множественного. Отдельный
            // разбор — отдельная задача, из сверки единственного числа исключаем.
            if (marks.contains("мн")) {
                pluralOnly++;
                continue;
            }
            nouns++;

            String headword = chunks.getFirst().text();
            // Многословные названия («Мала Азија», «Атлантски Океан») из сверки исключаем:
            // вторым фрагментом там идёт не падежная форма, а второе слово названия.
            if (bare(headword).contains(" ")) {
                continue;
            }
            String expected = genitiveFromSource(chunks, headword);
            if (expected == null) {
                continue;
            }
            withGenitive++;

            NounDeclension.Type type = NounDeclension.typeOf(headword, gender);
            int[] counters = byType.computeIfAbsent(type, key -> new int[2]);
            counters[0]++;

            String predicted = NounDeclension.genitiveSingular(headword, gender);
            List<String> candidates = NounDeclension.genitiveCandidates(headword, gender);
            candidateTotal += candidates.size();
            if (expected.equals(predicted)) {
                correct++;
                counters[1]++;
            } else if (candidates.contains(expected)) {
                // Верная форма нашлась среди дополнительных вариантов: беглое «а»,
                // наращение основы, переход л → о.
                correctViaExtended++;
                counters[1]++;
            } else {
                wrong++;
                if (wrongSamples.size() < SAMPLE_SIZE) {
                    wrongSamples.add("  %-16s %-14s род %-9s ждали %-14s вышло %s"
                            .formatted(row.name(), bare(headword), gender, expected, predicted));
                }
            }
        }

        int checked = correct + correctViaExtended + wrong;
        System.out.println("=== Склонение существительных: родительный падеж ===");
        System.out.printf("Существительных (есть помета рода): %d%n", nouns);
        System.out.printf("Из них указан родительный падеж:    %d%n", withGenitive);
        System.out.printf("Отложено (только мн. число):        %d%n%n", pluralOnly);

        System.out.printf("  верно основным правилом:   %6d  (%.1f%%)%n",
                correct, percent(correct, checked));
        System.out.printf("  верно доп. вариантом:      %6d  (%.1f%%)%n",
                correctViaExtended, percent(correctViaExtended, checked));
        System.out.printf("  ошиблись:                  %6d  (%.1f%%)%n",
                wrong, percent(wrong, checked));
        System.out.printf("%nИтоговая точность: %.1f%%%n",
                percent(correct + correctViaExtended, checked));
        System.out.printf("В среднем вариантов на слово: %.2f%n",
                checked == 0 ? 0 : (double) candidateTotal / checked);

        System.out.println("\n--- По типам склонения ---");
        System.out.printf("  %-22s %8s %8s %8s%n", "тип", "всего", "верно", "точность");
        byType.forEach((type, counters) -> System.out.printf("  %-22s %8d %8d %7.1f%%%n",
                type, counters[0], counters[1], percent(counters[1], counters[0])));

        System.out.println("\n--- Примеры ошибок ---");
        wrongSamples.forEach(System.out::println);
    }

    /**
     * Родительный падеж, указанный в статье: второй сербский фрагмент шапки.
     * Форма через тильду раскрывается в основу заглавного слова.
     */
    private static String genitiveFromSource(List<Chunk> chunks, String headword) {
        for (int i = 1; i < chunks.size() && i < 4; i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.isTranslation() || Chunk.SENSE_NUMBER.equals(chunk.tag())) {
                return null;
            }
            if (!Chunk.SERBIAN.equals(chunk.tag())) {
                continue;
            }
            String form = bare(Serbian.expandTilde(chunk.text(), headword));
            // Отсекаем варианты написания. Сравнение без учёта регистра: у имён
            // собственных вторым фрагментом идёт тот же заголовок со строчной буквы
            // (Африка, африка), и это не падежная форма.
            return form.equalsIgnoreCase(bare(headword)) ? null : form;
        }
        return null;
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

    private NounFormsReport() {
    }
}
