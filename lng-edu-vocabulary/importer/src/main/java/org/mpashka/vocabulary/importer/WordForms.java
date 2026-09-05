package org.mpashka.vocabulary.importer;

import org.mpashka.vocabulary.core.AdjectiveDeclension;
import org.mpashka.vocabulary.core.Chunk;
import org.mpashka.vocabulary.core.Entry;
import org.mpashka.vocabulary.core.Gender;
import org.mpashka.vocabulary.core.NounDeclension;
import org.mpashka.vocabulary.core.PartOfSpeech;
import org.mpashka.vocabulary.core.Serbian;
import org.mpashka.vocabulary.core.VerbConjugation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Собирает словоформы слова для указателя поиска.
 *
 * <p>Порождаются <b>только буквы</b> (без ударения): для поиска по любой форме этого
 * достаточно. Ударение к формам добавит этап доработки — из старого словаря, викисловаря
 * или языковой модели.
 *
 * <p>Формы даются набором вариантов (у сербских чередований часть словарная, а не
 * выводимая) — для указателя лишние варианты безвредны.
 */
// @tag:word-forms @tag:import
public final class WordForms {

    private WordForms() {
    }

    /**
     * Формы без ударения для поиска. Первой всегда идёт сама заглавная форма.
     *
     * @param entry  разобранная статья
     * @param gender род (для существительных); может быть {@code null}
     */
    public static List<String> searchForms(Entry entry, Gender gender, List<Chunk> chunks) {
        Set<String> forms = new LinkedHashSet<>();
        String headwordPlain = Serbian.stripCombiningAccents(entry.headword());
        forms.add(headwordPlain);

        switch (entry.partOfSpeech()) {
            case NOUN -> {
                if (gender != null) {
                    forms.addAll(NounDeclension.genitiveCandidates(headwordPlain, gender));
                }
            }
            case VERB -> forms.addAll(VerbConjugation.presentCandidates(headwordPlain));
            case ADJECTIVE -> {
                // У прилагательных формы родов выписаны в самой статье — берём готовыми.
                forms.addAll(AdjectiveDeclension.formsFromEntry(chunks));
                forms.addAll(AdjectiveDeclension.genderForms(headwordPlain));
            }
            default -> {
                // остальные части речи пока только по заглавной форме
            }
        }

        forms.removeIf(form -> form == null || form.isBlank());
        return List.copyOf(forms);
    }

    /** Есть ли смысл вообще строить формы для этой части речи. */
    public static boolean hasParadigm(PartOfSpeech partOfSpeech) {
        return partOfSpeech == PartOfSpeech.NOUN
                || partOfSpeech == PartOfSpeech.VERB
                || partOfSpeech == PartOfSpeech.ADJECTIVE;
    }
}
