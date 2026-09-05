package org.mpashka.vocabulary.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Формы прилагательных. Разметка подлинная, из исходной базы. */
class AdjectiveDeclensionTest {

    @Test
    @DisplayName("формы родов берутся готовыми из статьи, тильда раскрывается")
    void takesFormsFromEntry() {
        var chunks = MarkupParser.parse(
                "а‛псурд||ан$C#,#~ни_$C#,#~на$C#,#~но$C#абсу’рдный$RV#.#");

        assertThat(AdjectiveDeclension.formsFromEntry(chunks))
                .containsExactly("апсурдан", "апсурдни", "апсурдна", "апсурдно");
    }

    @Test
    @DisplayName("краткая форма: беглое «а» выпадает")
    void generatesFromShortForm() {
        assertThat(AdjectiveDeclension.genderForms("а‛псурд||ан"))
                .contains("апсурдни", "апсурдна", "апсурдно");
    }

    @Test
    @DisplayName("определённая форма на -и: окончание сперва снимается")
    void stripsDefiniteEnding() {
        // Иначе выходит «адјективнии».
        assertThat(AdjectiveDeclension.genderForms("адјекти’вни"))
                .contains("адјективна", "адјективно")
                .doesNotContain("адјективнии");
    }

    @Test
    @DisplayName("мягкая основа даёт средний род на -е")
    void softStemNeuterInE() {
        assertThat(AdjectiveDeclension.genderForms("би’вољ||и_")).contains("бивоље");
    }
}
