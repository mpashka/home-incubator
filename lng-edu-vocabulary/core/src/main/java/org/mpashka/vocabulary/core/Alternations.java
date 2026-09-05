package org.mpashka.vocabulary.core;

import java.util.Map;

/**
 * Чередования звуков на стыке основы и окончания.
 *
 * <p>Когда при склонении выпадает беглое «а», согласные основы и окончания оказываются
 * рядом и подстраиваются друг под друга:
 *
 * <pre>
 *   ба‛бак   → баб + ка   → ба‛пка     (озвончённая б оглушается перед к)
 *   безно‛жац → безнож + ца → безно‛шца  (ж → ш перед ц)
 *   бе‛дилац  → бедил + ца → бе‛диоца   (л на конце слога переходит в о)
 * </pre>
 */
// @tag:word-forms
public final class Alternations {

    /** Озвончённые согласные и их глухие пары. */
    private static final Map<Character, Character> DEVOICED = Map.of(
            'б', 'п',
            'г', 'к',
            'д', 'т',
            'ж', 'ш',
            'з', 'с',
            'ђ', 'ћ',
            'џ', 'ч');

    /** Глухие согласные, перед которыми происходит оглушение. */
    private static final String VOICELESS = "птксшцчћфх";

    private Alternations() {
    }

    /**
     * Применяет чередования на стыке основы и окончания.
     *
     * @param stem   основа после выпадения беглого «а»
     * @param ending окончание
     */
    public static String join(String stem, String ending) {
        if (stem.isEmpty() || ending.isEmpty()) {
            return stem + ending;
        }
        return devoice(stem, ending.charAt(0)) + ending;
    }

    /**
     * Переход {@code л → о} на конце слога: {@code бедил + ца → бедиоца}.
     *
     * <p><b>Это не правило, а словарная особенность.</b> Проверено на исходной базе:
     * при одинаковом строении одни слова переход дают ({@code бе‛дилац → бе‛диоца}),
     * а другие нет ({@code ба’јалац → ба’јалца}, {@code бе’лац → бе’лца},
     * {@code Ка’релац → Ка’релца}). Предсказать по написанию нельзя, поэтому переход
     * даётся <i>дополнительным вариантом</i>, а не применяется всегда.
     *
     * @return вариант с переходом либо {@code null}, если основа не оканчивается на «л»
     */
    public static String lToO(String stem, String ending) {
        if (!stem.endsWith("л") || ending.isEmpty() || !isConsonant(ending.charAt(0))) {
            return null;
        }
        return stem.substring(0, stem.length() - 1) + "о" + ending;
    }

    /** Оглушение последней согласной основы перед глухой согласной окончания. */
    private static String devoice(String stem, char next) {
        if (stem.isEmpty() || VOICELESS.indexOf(next) < 0) {
            return stem;
        }
        char last = stem.charAt(stem.length() - 1);
        Character voiceless = DEVOICED.get(last);
        return voiceless == null ? stem : stem.substring(0, stem.length() - 1) + voiceless;
    }

    /**
     * Йотация — смягчение последней согласной основы перед окончанием настоящего
     * времени на {@code -ем}:
     *
     * <pre>
     *   бау‛кати  → бау’чем   (к → ч)
     *   бене‛тати → бене’ћем  (т → ћ)
     *   ба“хтати  → ба’шћем   (х → ш)
     * </pre>
     *
     * @return основа со смягчённой согласной либо {@code null}, если смягчать нечего
     */
    public static String palatalize(String stem) {
        if (stem.isEmpty()) {
            return null;
        }
        // Сочетания смягчаются целиком: би‛скати → би’штем, ба“хтати → ба’шћем.
        for (var pair : PALATALIZED_CLUSTERS.entrySet()) {
            if (stem.endsWith(pair.getKey())) {
                return stem.substring(0, stem.length() - pair.getKey().length()) + pair.getValue();
            }
        }
        char last = stem.charAt(stem.length() - 1);
        Character softened = PALATALIZED.get(last);
        return softened == null ? null : stem.substring(0, stem.length() - 1) + softened;
    }

    /** Сочетания согласных, смягчающиеся целиком. Проверяются раньше одиночных. */
    private static final Map<String, String> PALATALIZED_CLUSTERS = Map.of(
            "ск", "шт",
            "ст", "шт",
            "зд", "жд",
            "хт", "шћ",
            "зг", "жд");

    /**
     * Согласные и их смягчённые пары при йотации.
     *
     * <p>Губные {@code б}, {@code п}, {@code м}, {@code в} сюда не входят: они дают
     * двухбуквенные сочетания ({@code бљ}, {@code пљ}, {@code мљ}, {@code вљ}),
     * а это уже не замена буквы на букву. Их черёд — вместе с остальной парадигмой.
     */
    private static final Map<Character, Character> PALATALIZED = Map.ofEntries(
            Map.entry('к', 'ч'),
            Map.entry('г', 'ж'),
            Map.entry('х', 'ш'),
            Map.entry('ц', 'ч'),
            Map.entry('з', 'ж'),
            Map.entry('с', 'ш'),
            Map.entry('т', 'ћ'),
            Map.entry('д', 'ђ'),
            Map.entry('л', 'љ'),
            Map.entry('н', 'њ'));

    private static boolean isConsonant(char letter) {
        return Character.isLetter(letter) && "аеиоу".indexOf(letter) < 0;
    }
}
