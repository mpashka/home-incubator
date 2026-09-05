package org.mpashka.vocabulary.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Спряжение глаголов. Все пары «инфинитив → настоящее время» взяты из исходной базы.
 */
class VerbConjugationTest {

    @Nested
    @DisplayName("Основные окончания")
    class Patterns {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "ра’дити,        радим",
                "а“бати,         абам",
                "аисну‛ти,       аиснем",
                "абдици’рати,    абдицирам",
                "адвокати’сати,  адвокатишем",
                "аванзова‛ти,    аванзујем",
                "бљу‛вати,       бљујем",
        })
        @DisplayName("настоящее время выводится по окончанию инфинитива")
        void conjugates(String infinitive, String expected) {
            assertThat(VerbConjugation.presentFirstSingular(infinitive)).isEqualTo(expected);
        }

        @Test
        @DisplayName("более длинное окончание проверяется раньше короткого")
        void longerPatternWins() {
            // -овати должно разбираться как -овати, а не как -ати.
            assertThat(VerbConjugation.presentFirstSingular("аванзова‛ти")).isEqualTo("аванзујем");
        }

        @Test
        @DisplayName("возвратная частица сохраняется")
        void keepsReflexiveParticle() {
            assertThat(VerbConjugation.presentFirstSingular("а“кати се")).isEqualTo("акам се");
        }

        @Test
        @DisplayName("не инфинитив — формы нет")
        void notAnInfinitive() {
            assertThat(VerbConjugation.presentFirstSingular("аба‛жу_р")).isNull();
        }
    }

    @Nested
    @DisplayName("Йотация")
    class Palatalization {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "бау‛кати,   баучем",   // к → ч
                "бене‛тати,  бенећем",  // т → ћ
                "би‛скати,   биштем",   // ск → шт, сочетание целиком
        })
        @DisplayName("согласная основы смягчается перед -ем")
        void softensBeforeEm(String infinitive, String expected) {
            assertThat(VerbConjugation.presentCandidates(infinitive)).contains(expected);
        }

        @Test
        @DisplayName("сочетание согласных смягчается раньше одиночной")
        void clusterBeatsSingleConsonant() {
            assertThat(Alternations.palatalize("биск")).isEqualTo("бишт");
            assertThat(Alternations.palatalize("баук")).isEqualTo("бауч");
        }

        @Test
        @DisplayName("губные согласные не смягчаются — они дают двухбуквенные сочетания")
        void labialsAreLeftAlone() {
            assertThat(Alternations.palatalize("зоб")).isNull();
        }
    }

    @Nested
    @DisplayName("Сокращённая запись формы")
    class Abbreviations {

        @ParameterizedTest(name = "{0} + {1} → {2}")
        @CsvSource({
                "акцентова‛ти,   тујем,  акцентујем",
                "аванзова‛ти,    зујем,  аванзујем",
                "администрова‛ти, рујем, администрујем",
                "аискати,        кам,    аискам",
                "биберити,       рим,    биберим",
        })
        @DisplayName("начало формы восстанавливается по инфинитиву")
        void expands(String infinitive, String tail, String expected) {
            assertThat(VerbConjugation.expandAbbreviated(infinitive, tail)).isEqualTo(expected);
        }

        @Test
        @DisplayName("стык ищется у конца слова, а не по первому совпадению буквы")
        void joinsNearTheEnd() {
            // Буква «т» есть и в середине «акцентовати», и в «-вати». Взять последнюю —
            // значит получить «акцентоватујем».
            assertThat(VerbConjugation.expandAbbreviated("акцентова‛ти", "тујем"))
                    .isEqualTo("акцентујем");
        }
    }
}
