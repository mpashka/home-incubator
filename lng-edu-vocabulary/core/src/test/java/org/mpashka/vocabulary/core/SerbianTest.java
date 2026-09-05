package org.mpashka.vocabulary.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SerbianTest {

    /**
     * Соответствие знаков ударения сербской норме. Слова взяты из исходной базы,
     * норма — из сербской акцентологии.
     */
    @ParameterizedTest(name = "{0} — {2}")
    @CsvSource({
            "vo‛da,   vòda,  краткое восходящее",
            "ru’ka,   rúka,  долгое восходящее",
            "bra“t,   brȁt,  краткое нисходящее",
            "ma^jka,  mȃjka, долгое нисходящее",
            "ju‛na_k, jùnāk, краткое восходящее и заударная долгота",
    })
    @DisplayName("знаки ударения переходят в комбинируемые знаки Unicode")
    void rendersAccents(String source, String expected, String description) {
        assertThat(Serbian.renderAccents(source))
                .as(description)
                .isEqualTo(java.text.Normalizer.normalize(expected, java.text.Normalizer.Form.NFD));
    }

    @Test
    @DisplayName("кириллица переходит в латиницу, диграфы разворачиваются")
    void transliterates() {
        assertThat(Serbian.toLatin("вода")).isEqualTo("voda");
        assertThat(Serbian.toLatin("љубав")).isEqualTo("ljubav");
        assertThat(Serbian.toLatin("њива")).isEqualTo("njiva");
        assertThat(Serbian.toLatin("џеп")).isEqualTo("džep");
        assertThat(Serbian.toLatin("ђак")).isEqualTo("đak");
        assertThat(Serbian.toLatin("Београд")).isEqualTo("Beograd");
    }

    @Test
    @DisplayName("знаки ударения проходят транслитерацию насквозь")
    void keepsAccentsWhileTransliterating() {
        assertThat(Serbian.toLatin("во‛да")).isEqualTo("vo‛da");
    }

    @Test
    @DisplayName("тильда заменяет основу — часть до ||")
    void expandsTildeToStem() {
        assertThat(Serbian.expandTilde("~а", "во‛д||а")).isEqualTo("во‛да");
        assertThat(Serbian.expandTilde("слатка ~а", "во‛д||а")).isEqualTo("слатка во‛да");
    }

    @Test
    @DisplayName("без || тильда заменяет слово целиком")
    void expandsTildeToWholeWordWithoutStemMarker() {
        assertThat(Serbian.expandTilde("~ се", "ра’дити")).isEqualTo("ра’дити се");
    }

    @Test
    @DisplayName("знаки ударения снимаются")
    void stripsAccents() {
        assertThat(Serbian.stripAccents("ju‛na_k")).isEqualTo("junak");
    }
}
