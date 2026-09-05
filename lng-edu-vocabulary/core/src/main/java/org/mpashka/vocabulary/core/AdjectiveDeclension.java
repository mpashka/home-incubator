package org.mpashka.vocabulary.core;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Формы прилагательного по родам.
 *
 * <p><b>Важно: у прилагательных эти формы в исходной базе уже есть.</b> Словарь указывает
 * их сразу после заглавного слова ({@code абеце’д||ан, ~ни_, ~на, ~но}), поэтому при
 * переносе данных их надо <b>забирать готовыми</b>, а не выводить правилами. Порождение
 * нужно лишь там, где словарь форму опустил.
 */
// @tag:word-forms
public final class AdjectiveDeclension {

    private AdjectiveDeclension() {
    }

    /**
     * Формы, выписанные в самой статье: определённая мужского рода, женского, среднего.
     * Тильда раскрывается в основу заглавного слова.
     *
     * @return формы в том порядке, в каком они стоят в статье
     */
    public static List<String> formsFromEntry(List<Chunk> chunks) {
        Set<String> forms = new LinkedHashSet<>();
        if (chunks.isEmpty()) {
            return List.of();
        }
        String headword = chunks.getFirst().text();
        // Само заглавное слово — тоже форма (краткая мужского рода), поэтому идёт первым.
        forms.add(Serbian.stripAccents(Serbian.stripStemMarker(headword)).trim());
        for (int i = 1; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.isTranslation() || Chunk.SENSE_NUMBER.equals(chunk.tag())) {
                break;
            }
            if (!Chunk.SERBIAN.equals(chunk.tag())) {
                continue;
            }
            String form = Serbian.stripAccents(
                    Serbian.stripStemMarker(Serbian.expandTilde(chunk.text(), headword))).trim();
            if (!form.isEmpty()) {
                forms.add(form);
            }
        }
        return List.copyOf(forms);
    }

    /**
     * Порождённые формы родов для случая, когда словарь их не выписал.
     *
     * <p>Прилагательное даётся в краткой форме мужского рода. Женский и средний
     * получаются прибавлением окончания, причём у слов с беглым «а» оно выпадает:
     * {@code абеце’дан → абеце’дна, абеце’дно}.
     */
    public static List<String> genderForms(String headword) {
        String bare = Serbian.stripAccents(Serbian.stripStemMarker(headword)).trim();
        if (bare.isEmpty()) {
            return List.of();
        }
        Set<String> forms = new LinkedHashSet<>();

        // Заглавное слово бывает и в краткой форме (абеце’дан), и в определённой
        // (адјекти’вни). Во втором случае окончание надо сперва снять, иначе выйдет
        // «адјективнии».
        String stem = bare.endsWith("и") ? bare.substring(0, bare.length() - 1) : bare;
        addForms(forms, stem);

        // Беглое «а» последнего слога: абеце’дан → абеце’дн- + окончание.
        String withoutFleeting = dropFleetingA(stem);
        if (withoutFleeting != null) {
            addForms(forms, withoutFleeting);
        }
        return List.copyOf(forms);
    }

    private static void addForms(Set<String> target, String stem) {
        target.add(stem + "и");
        target.add(stem + "а");
        target.add(stem + "о");
        // Мягкая основа даёт средний род на -е: ба“бљи → ба“бље.
        target.add(stem + "е");
    }

    /** Убирает беглое «а» последнего слога: {@code абецедан → абецедн}. */
    private static String dropFleetingA(String bare) {
        if (bare.length() < 3) {
            return null;
        }
        int last = bare.length() - 1;
        boolean pattern = isConsonant(bare.charAt(last))
                && bare.charAt(last - 1) == 'а'
                && isConsonant(bare.charAt(last - 2));
        return pattern ? bare.substring(0, last - 1) + bare.charAt(last) : null;
    }

    private static boolean isConsonant(char letter) {
        return Character.isLetter(letter) && "аеиоу".indexOf(letter) < 0;
    }
}
