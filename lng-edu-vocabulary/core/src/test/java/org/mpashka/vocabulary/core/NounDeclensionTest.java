package org.mpashka.vocabulary.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Склонение существительных. Все примеры взяты из исходной базы вместе с указанным
 * там родительным падежом, поэтому проверки сверяются с настоящим словарём,
 * а не с моим представлением о сербском языке.
 */
class NounDeclensionTest {

    @Nested
    @DisplayName("Основные типы склонения")
    class MainTypes {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "аба‛жу_р,      абажура,   MASCULINE",
                "во‛д||а,       воде,      FEMININE",
                "бе‛збедн||ост, безбедности, FEMININE",
                "по’ље,         поља,      NEUTER",
        })
        @DisplayName("родительный падеж выводится основным правилом")
        void basicGenitive(String headword, String expected, Gender gender) {
            assertThat(NounDeclension.genitiveSingular(headword, gender)).isEqualTo(expected);
        }

        @Test
        @DisplayName("без пометы рода тип склонения неизвестен")
        void unknownWithoutGender() {
            assertThat(NounDeclension.typeOf("аба‛жу_р", null)).isEqualTo(NounDeclension.Type.UNKNOWN);
            assertThat(NounDeclension.genitiveSingular("аба‛жу_р", null)).isNull();
        }
    }

    @Test
    @DisplayName("регулярная парадигма даёт 7 падежей в двух числах")
    void regularParadigm() {
        assertThat(NounDeclension.regularParadigm("зид", Gender.MASCULINE))
                .containsExactly(
                        new Form("nom.sg", "зид"), new Form("gen.sg", "зида"),
                        new Form("dat.sg", "зиду"), new Form("acc.sg", "зид"),
                        new Form("voc.sg", "зиде"), new Form("ins.sg", "зидом"),
                        new Form("loc.sg", "зиду"), new Form("nom.pl", "зидови"),
                        new Form("gen.pl", "зидова"), new Form("dat.pl", "зидовима"),
                        new Form("acc.pl", "зидове"), new Form("voc.pl", "зидови"),
                        new Form("ins.pl", "зидовима"), new Form("loc.pl", "зидовима"));
        assertThat(NounDeclension.regularParadigm("вода", Gender.FEMININE)).hasSize(14);
        assertThat(NounDeclension.regularParadigm("село", Gender.NEUTER)).hasSize(14);
    }

    @Nested
    @DisplayName("Беглое «а»")
    class FleetingA {

        @Test
        @DisplayName("гласная последнего слога выпадает")
        void dropsVowel() {
            assertThat(NounDeclension.genitiveFleeting("Абисина‛ц")).isEqualTo("Абисинца");
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "ба“бак,     бапка",      // б оглушается перед к
                "безно‛жац,  безношца",   // ж → ш перед ц
                "бакроре‛зац, бакроресца", // з → с перед ц
        })
        @DisplayName("согласная основы оглушается перед глухой согласной окончания")
        void devoicesBeforeVoiceless(String headword, String expected) {
            assertThat(NounDeclension.genitiveFleeting(headword)).isEqualTo(expected);
        }

        @Test
        @DisplayName("слово без строения «согласная + а + согласная» правила не даёт")
        void requiresConsonantVowelConsonant() {
            assertThat(NounDeclension.genitiveFleeting("во‛да")).isNull();
        }
    }

    @Nested
    @DisplayName("Несколько вариантов формы")
    class Candidates {

        @Test
        @DisplayName("переход л → о даётся вариантом, а не применяется всегда")
        void lToOIsOnlyACandidate() {
            // При одинаковом строении словарь даёт разное: бе’дилац → бе’диоца,
            // но ба’јалац → ба’јалца. Предсказать нельзя, поэтому оба варианта.
            assertThat(NounDeclension.genitiveCandidates("бе‛дилац", Gender.MASCULINE))
                    .contains("бедиоца", "бедилца");
            assertThat(NounDeclension.genitiveCandidates("ба’јалац", Gender.MASCULINE))
                    .contains("бајалца");
        }

        @Test
        @DisplayName("средний род: оба наращения основы, -ет- и -ен-")
        void bothNeuterExtensions() {
            assertThat(NounDeclension.genitiveCandidates("де‛те", Gender.NEUTER))
                    .contains("детета");
            assertThat(NounDeclension.genitiveCandidates("бре“ме", Gender.NEUTER))
                    .contains("бремена");
        }

        @Test
        @DisplayName("вариантов немного — указатель словоформ не разбухает")
        void candidateSetStaysSmall() {
            // По всей базе в среднем 1,9 варианта на слово (:importer:runNounForms).
            assertThat(NounDeclension.genitiveCandidates("аба‛жу_р", Gender.MASCULINE)).hasSizeLessThan(4);
            assertThat(NounDeclension.genitiveCandidates("во‛д||а", Gender.FEMININE)).hasSizeLessThan(4);
        }
    }

    @Nested
    @DisplayName("Чередования")
    class AlternationRules {

        @Test
        @DisplayName("оглушение только перед глухой согласной")
        void devoicesOnlyBeforeVoiceless() {
            assertThat(Alternations.join("безнож", "ца")).isEqualTo("безношца");
            assertThat(Alternations.join("безнож", "ба")).isEqualTo("безножба");
        }

        @Test
        @DisplayName("переход л → о даётся отдельно и только для основы на «л»")
        void lToOAppliesToLStemsOnly() {
            assertThat(Alternations.lToO("бедил", "ца")).isEqualTo("бедиоца");
            assertThat(Alternations.lToO("безнож", "ца")).isNull();
        }
    }
}
