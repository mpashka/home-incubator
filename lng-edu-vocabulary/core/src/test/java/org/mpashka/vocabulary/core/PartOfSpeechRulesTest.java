package org.mpashka.vocabulary.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Правила определения части речи. Разметка в проверках подлинная, из {@code srbbase.db}.
 *
 * <p>Числа качества считает {@code :importer:run}, здесь закреплены сами правила
 * и разобранные краевые случаи, чтобы они не сломались молча.
 */
class PartOfSpeechRulesTest {

    private static PartOfSpeech detect(String markup) {
        List<Chunk> chunks = MarkupParser.parse(markup);
        return PartOfSpeechRules.detect(EntryParser.collectMarks(chunks), chunks);
    }

    private static PartOfSpeech byStructure(String markup) {
        return PartOfSpeechRules.detectByStructure(MarkupParser.parse(markup));
    }

    @Nested
    @DisplayName("По явной помете")
    class ByMark {

        @ParameterizedTest(name = "{1} → {2}")
        @CsvSource({
                "'во‛д||а$C#ж$#.#вода’$RV#.#',        ж,     NOUN",
                "'а‛банос$C#м$#.#дерево$RV#.#',       м,     NOUN",
                "'бр“зо$C#нареч$#.#бы’стро$RV#.#',    нареч, ADVERB",
                "'а‛$C#межд$#.#ах!$RV#.#',            межд,  INTERJECTION",
        })
        @DisplayName("помета называет часть речи напрямую")
        void detectsByMark(String markup, String mark, PartOfSpeech expected) {
            assertThat(detect(markup)).isEqualTo(expected);
        }

        @Test
        @DisplayName("«ср» — это «сравни», а не средний род")
        void sravniIsNotNeuter() {
            // Средний род в этом словаре обозначен через «с», а «ср» значит «сравни».
            assertThat(detect("а“баџиски$C#портня’жный$RV#;#ср$#.#абаџија$CL#.#"))
                    .isNotEqualTo(PartOfSpeech.NOUN);
        }

        @Test
        @DisplayName("«с» из сокращения «с.-х.» не читается как средний род")
        void compoundAbbreviationIsNotAGenderMark() {
            // Настоящая статья: «мед., с.-х. прививать» — это глагол, а не существительное.
            String markup = "це’пити$C#,#це^пи_м$C#мед$#.#,#с$#.#-#х$#.#привива’ть$RV#.#";

            assertThat(EntryParser.collectMarks(MarkupParser.parse(markup))).doesNotContain("с");
            assertThat(detect(markup)).isEqualTo(PartOfSpeech.VERB);
        }
    }

    @Nested
    @DisplayName("Глагол — по инфинитиву")
    class Verbs {

        @ParameterizedTest(name = "{0}")
        @CsvSource({
                "'ра’дити$C#,#ра^ди_м$C#рабо’тать$RV#.#'",
                "'а“кати се$C#,#а“ка_м се$C#вози’ться$RV#.#'",
                "'ре“ћи$C#,#ре“че_м$C#сказа’ть$RV#.#'",
        })
        @DisplayName("инфинитив на -ти/-ћи, в том числе возвратный")
        void detectsInfinitive(String markup) {
            assertThat(byStructure(markup)).isEqualTo(PartOfSpeech.VERB);
        }

        @Test
        @DisplayName("вариант написания в скобках не выдаёт слово за глагол")
        void variantInParenthesesIsNotAVerbForm() {
            // Прежнее правило «вторая форма на -м» объявляло Амстердам глаголом.
            assertThat(byStructure("Амстердам$C#(#а’мстердам$C#)#м$#.#Амстерда’м$RV#.#"))
                    .isNotEqualTo(PartOfSpeech.VERB);
        }

        @Test
        @DisplayName("наречие с двумя вариантами ударения — не глагол")
        void accentVariantIsNotAVerbForm() {
            assertThat(byStructure("ба“рем$C#,#ба^рем$C#нареч$#.#по$RV#кра’йней$RV#ме’ре$RV#.#"))
                    .isNotEqualTo(PartOfSpeech.VERB);
        }
    }

    @Nested
    @DisplayName("Прилагательное — по формам родов")
    class Adjectives {

        @Test
        @DisplayName("три формы через тильду")
        void detectsGenderForms() {
            assertThat(byStructure("а‛псурд||ан$C#,#~ни_$C#,#~на$C#,#~но$C#абсу’рдный$RV#.#"))
                    .isEqualTo(PartOfSpeech.ADJECTIVE);
        }

        @Test
        @DisplayName("мягкое прилагательное: средний род на -е, а не на -о")
        void detectsSoftStemNeuterInE() {
            assertThat(byStructure("би’вољ||и_$C#,#~а_$C#,#~е_$C#бу’йволовый$RV#.#"))
                    .isEqualTo(PartOfSpeech.ADJECTIVE);
        }

        @Test
        @DisplayName("притяжательное без формы мужского рода на -и")
        void detectsPossessiveWithoutMasculineForm() {
            assertThat(byStructure("да’дин$C#,#~а$C#,#~о$C#ма’менькин$RV#.#"))
                    .isEqualTo(PartOfSpeech.ADJECTIVE);
        }

        @Test
        @DisplayName("падежная форма существительного через тильду — не прилагательное")
        void nounCaseFormIsNotAnAdjective() {
            // Одной лишь тильды мало: у существительных так пишут родительный падеж.
            assertThat(byStructure("бе‛збедн||о_ст$C#,#~ости$C#ж$#.#безопа’сность$RV#.#"))
                    .isNotEqualTo(PartOfSpeech.ADJECTIVE);
            assertThat(byStructure("де‛те_нц||е$C#,#~ета$C#с$#.#ди’тятко$RV#.#"))
                    .isNotEqualTo(PartOfSpeech.ADJECTIVE);
        }
    }

    @Nested
    @DisplayName("Порядок правил")
    class Priority {

        @Test
        @DisplayName("явная помета сильнее строения")
        void markWinsOverStructure() {
            // Множественное «Карпати» оканчивается на -ти, но помечено родом.
            String markup = "Ка^рпати$C#мн$#.#м$#.#Карпа’ты$RV#.#";

            assertThat(byStructure(markup)).isEqualTo(PartOfSpeech.VERB);
            assertThat(detect(markup)).isEqualTo(PartOfSpeech.NOUN);
        }
    }
}
