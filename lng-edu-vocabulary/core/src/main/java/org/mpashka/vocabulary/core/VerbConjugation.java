package org.mpashka.vocabulary.core;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Спряжение глаголов: первое лицо единственного числа настоящего времени.
 *
 * <p>Эта форма выбрана опорной потому, что исходная база указывает её сразу после
 * инфинитива ({@code ра’дити, ра^ди_м}) — готовый образец для сверки, ничего размечать
 * вручную не требуется. Она же задаёт основу настоящего времени, от которой строятся
 * остальные лица.
 *
 * <p><b>Ударение не выводится</b> — только буквы. Обоснование в
 * [docs/implementation/word-forms.md]: ударение словоформы совпадает с ударением заглавного слова
 * лишь в 16,4 % случаев.
 */
// @tag:word-forms
public final class VerbConjugation {

    /**
     * Окончания инфинитива и соответствующие окончания первого лица настоящего времени.
     * Порядок важен: более длинные окончания проверяются раньше, иначе {@code -овати}
     * будет разобрано как {@code -ати}.
     */
    private static final String[][] PATTERNS = {
            {"овати", "ујем"},   // аванзова‛ти → аванзу’јем
            {"евати", "ујем"},
            {"ивати", "ујем"},
            {"увати", "ујем"},   // бљу‛вати → бљу’јем
            {"ирати", "ирам"},   // абдици’рати → абдици’рам
            {"исати", "ишем"},   // адвокати’сати → адвокати’шем
            {"нути", "нем"},     // аисну’ти → аисне’м
            {"јети", "им"},
            {"ети", "им"},       // ви’дети → ви’дим
            {"ити", "им"},       // ра’дити → ра^ди_м
            {"ати", "ам"},       // а“бати → а“ба_м
            {"ути", "ем"},
            {"сти", "дем"},
            {"ћи", "чем"},       // ре“ћи → ре“че_м
    };

    private VerbConjugation() {
    }

    /**
     * Первое лицо единственного числа настоящего времени — основной вариант.
     *
     * @param infinitive инфинитив кириллицей, знаки ударения допустимы
     * @return форма без знаков ударения либо {@code null}, если слово не похоже на инфинитив
     */
    public static String presentFirstSingular(String infinitive) {
        String bare = bare(infinitive);
        boolean reflexive = bare.endsWith(" се");
        if (reflexive) {
            bare = bare.substring(0, bare.length() - 3).trim();
        }
        for (String[] pattern : PATTERNS) {
            if (bare.endsWith(pattern[0])) {
                String form = bare.substring(0, bare.length() - pattern[0].length()) + pattern[1];
                return reflexive ? form + " се" : form;
            }
        }
        return null;
    }

    /**
     * Все правдоподобные варианты формы настоящего времени.
     *
     * <p>Часть глаголов спрягается не по написанию, а по своему типу, который из букв
     * не виден: {@code -ати} даёт и {@code -ам} ({@code а“бати → а“ба_м}), и {@code -ем}
     * ({@code писа’ти → пи’ше_м}). Поэтому вариантов несколько — для указателя словоформ
     * этого достаточно, а единственно верный выбирает языковая модель.
     */
    public static List<String> presentCandidates(String infinitive) {
        Set<String> candidates = new LinkedHashSet<>();
        String main = presentFirstSingular(infinitive);
        if (main != null) {
            candidates.add(main);
        }
        String bare = bare(infinitive);
        boolean reflexive = bare.endsWith(" се");
        if (reflexive) {
            bare = bare.substring(0, bare.length() - 3).trim();
        }
        // Глаголы на -ати и -ети образуют настоящее время двояко.
        if (bare.endsWith("ати") || bare.endsWith("ети")) {
            String stem = bare.substring(0, bare.length() - 3);
            add(candidates, stem + "ем", reflexive);
            add(candidates, stem + "им", reflexive);
            // Перед -ем согласная основы смягчается: бау‛кати → бау’чем.
            String softened = Alternations.palatalize(stem);
            if (softened != null) {
                add(candidates, softened + "ем", reflexive);
            }
        }
        // -ивати и -евати дают и -ујем, и простое -ивам / -евам.
        if (bare.endsWith("ивати") || bare.endsWith("евати")) {
            add(candidates, bare.substring(0, bare.length() - 3) + "ам", reflexive);
        }
        // Глаголы на -ћи и -сти спрягаются по основе, которой в инфинитиве не видно:
        // ре“ћи → ре“че_м, али сти‛ћи → сти“гне_м; кра‛сти → кра“де_м, али па‛сти → па“дне_м.
        if (bare.endsWith("ћи")) {
            String stem = bare.substring(0, bare.length() - 2);
            add(candidates, stem + "жем", reflexive);
            add(candidates, stem + "гнем", reflexive);
            add(candidates, stem + "ђем", reflexive);
            add(candidates, stem + "ем", reflexive);
        }
        if (bare.endsWith("сти")) {
            String stem = bare.substring(0, bare.length() - 3);
            add(candidates, stem + "дем", reflexive);
            add(candidates, stem + "днем", reflexive);
            add(candidates, stem + "зем", reflexive);
            add(candidates, stem + "тем", reflexive);
            add(candidates, stem + "нем", reflexive);
        }
        return List.copyOf(candidates);
    }

    /**
     * Восстанавливает форму, записанную в словаре сокращённо.
     *
     * <p>Словарь опускает начало формы, оставляя только хвост:
     * {@code аванзова‛ти, -зу’јем} означает {@code аванзу’јем}. Начало берётся из
     * инфинитива по последнему вхождению первой буквы хвоста.
     *
     * @return восстановленная форма либо сам хвост, если совместить не удалось
     */
    public static String expandAbbreviated(String infinitive, String tail) {
        String bareInfinitive = bare(infinitive);
        String bareTail = bare(tail);
        while (bareTail.startsWith("-")) {
            bareTail = bareTail.substring(1);
        }
        if (bareTail.isEmpty()) {
            return bareTail;
        }
        // Стык ищем там, где длина формы примерно совпадает с длиной инфинитива:
        // словарь опускает начало, а не середину. Из нескольких вхождений первой
        // буквы хвоста берём ближайшее к этому месту — иначе «акцентова‛ти, -ту’јем»
        // склеится в «акцентоватујем» вместо «акценту’јем».
        int target = bareInfinitive.length() - bareTail.length();
        int best = -1;
        for (int i = 0; i < bareInfinitive.length(); i++) {
            if (bareInfinitive.charAt(i) == bareTail.charAt(0)
                    && (best < 0 || Math.abs(i - target) < Math.abs(best - target))) {
                best = i;
            }
        }
        return best <= 0 ? bareTail : bareInfinitive.substring(0, best) + bareTail;
    }

    private static void add(Set<String> target, String form, boolean reflexive) {
        target.add(reflexive ? form + " се" : form);
    }

    private static String bare(String text) {
        return Serbian.stripAccents(Serbian.stripStemMarker(text)).trim();
    }
}
