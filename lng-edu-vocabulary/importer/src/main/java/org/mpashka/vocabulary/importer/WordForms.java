package org.mpashka.vocabulary.importer;

import org.mpashka.vocabulary.core.AdjectiveDeclension;
import org.mpashka.vocabulary.core.Chunk;
import org.mpashka.vocabulary.core.Entry;
import org.mpashka.vocabulary.core.Form;
import org.mpashka.vocabulary.core.Gender;
import org.mpashka.vocabulary.core.NounDeclension;
import org.mpashka.vocabulary.core.PartOfSpeech;
import org.mpashka.vocabulary.core.Serbian;
import org.mpashka.vocabulary.core.VerbConjugation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * <p>У каждой формы есть <b>грамматическая помета</b>: без неё поиск умеет только
     * сказать «нашлось по форме», но не может объяснить, в каком падеже слово так
     * выглядит. Помета — та, по которой форма порождена, а не разобранная задним числом.
     *
     * <p>Одна и та же буквенная форма встречается в парадигме не раз ({@code вода} —
     * и родительный единственного, и именительный множественного у другого типа):
     * в списке она остаётся один раз, с первой пометой, потому что указателю нужна
     * строка, а не полный набор её ролей.
     *
     * @param entry  разобранная статья
     * @param gender род (для существительных); может быть {@code null}
     */
    public static List<Form> searchForms(Entry entry, Gender gender, List<Chunk> chunks) {
        Map<String, Form> forms = new LinkedHashMap<>();
        String headwordPlain = Serbian.stripCombiningAccents(entry.headword());
        add(forms, "nom.sg", headwordPlain);

        switch (entry.partOfSpeech()) {
            case NOUN -> {
                if (gender != null) {
                    // Полная регулярная парадигма: искать надо по любому падежу, а не
                    // только по родительному.
                    for (Form form : NounDeclension.regularParadigm(headwordPlain, gender)) {
                        add(forms, form.grammar(), form.value());
                    }
                    // Варианты родительного: чередования дают несколько кандидатов,
                    // и какой из них словарный — правилами не решается.
                    for (String candidate : NounDeclension.genitiveCandidates(headwordPlain, gender)) {
                        add(forms, "gen.sg", candidate);
                    }
                }
            }
            case VERB -> {
                for (String candidate : VerbConjugation.presentCandidates(headwordPlain)) {
                    add(forms, "praes.1sg", candidate);
                }
            }
            case ADJECTIVE -> {
                // У прилагательных формы родов выписаны в самой статье — берём готовыми.
                // Какому роду какая форма отвечает, из статьи не следует, поэтому помета
                // общая: «форма прилагательного».
                for (String form : AdjectiveDeclension.formsFromEntry(chunks)) {
                    add(forms, "adj", form);
                }
                for (String form : AdjectiveDeclension.genderForms(headwordPlain)) {
                    add(forms, "adj", form);
                }
            }
            default -> {
                // остальные части речи пока только по заглавной форме
            }
        }

        return List.copyOf(forms.values());
    }

    private static void add(Map<String, Form> forms, String grammar, String value) {
        if (value != null && !value.isBlank()) {
            forms.putIfAbsent(value, new Form(grammar, value));
        }
    }

    /** Есть ли смысл вообще строить формы для этой части речи. */
    public static boolean hasParadigm(PartOfSpeech partOfSpeech) {
        return partOfSpeech == PartOfSpeech.NOUN
                || partOfSpeech == PartOfSpeech.VERB
                || partOfSpeech == PartOfSpeech.ADJECTIVE;
    }
}
