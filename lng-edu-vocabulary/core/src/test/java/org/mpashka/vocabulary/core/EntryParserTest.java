package org.mpashka.vocabulary.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Разбор статей исходной базы. Разметка в проверках — настоящая, скопирована
 * из {@code srbbase.db}, чтобы проверки не разошлись с действительностью.
 */
class EntryParserTest {

    /** Статья «вода»: два значения, примеры с тильдой и блок устойчивых оборотов. */
    private static final String VODA = """
            во‛д||а$C#ж$#.#1$D#)#вода’$RV#;#~а$C#за$CL#пиће$CL#питьева’я$#вода’$#;#\
            слатка$C#~а$C#пре’сная$#вода’$#;#2$D#)#река’$RV#;#\
            ◊$S#велика$C#~а$C#полово’дье$#,#разли’в$#.#""";

    @Nested
    @DisplayName("Заглавное слово и ударение")
    class Headword {

        @Test
        @DisplayName("знаки ударения становятся комбинируемыми, служебный || снимается")
        void rendersAccents() {
            Entry entry = EntryParser.parse("voda", "вода’;река’", VODA);

            assertThat(entry.headword()).isEqualTo("во̀да");
            assertThat(entry.headwordLatin()).isEqualTo("vòda");
        }
    }

    @Nested
    @DisplayName("Значения и переводы")
    class Senses {

        @Test
        @DisplayName("нумерованные значения разделяются")
        void splitsNumberedSenses() {
            Entry entry = EntryParser.parse("voda", "вода’;река’", VODA);

            assertThat(entry.senses()).hasSize(2);
            assertThat(entry.senses().get(0).number()).isEqualTo(1);
            assertThat(entry.senses().get(0).translations()).containsExactly("вода́");
            assertThat(entry.senses().get(1).number()).isEqualTo(2);
            assertThat(entry.senses().get(1).translations()).containsExactly("река́");
        }

        @Test
        @DisplayName("статья без нумерации даёт одно значение")
        void singleSenseWhenNotNumbered() {
            Entry entry = EntryParser.parse("aba", "аба’",
                    "а‛ба$C#ж$#.#уст$#.#аба’$RV#.#");

            assertThat(entry.senses()).hasSize(1);
            assertThat(entry.senses().getFirst().number()).isNull();
            assertThat(entry.senses().getFirst().translations()).containsExactly("аба́");
        }

        @Test
        @DisplayName("у отсылочной статьи переводы берутся из kw — фрагментов RV в ней нет")
        void referenceEntryFallsBackToKeywords() {
            Entry entry = EntryParser.parse("abanos", "чёрное де’рево;эбе’новое де’рево",
                    "а‛банос$C#м$#.#см$#.#абонос$CL#.#");

            assertThat(entry.senses()).hasSize(1);
            assertThat(entry.senses().getFirst().translations())
                    .containsExactly("чёрное де́рево", "эбе́новое де́рево");
        }

        @Test
        @DisplayName("запятая не рвёт перевод, если в kw это одно целое")
        void keywordsDecideCommaBoundary() {
            // Запятая здесь помечена PRV, а не идёт без пометы — разбор обязан
            // обрабатывать оба вида разделителя одинаково.
            Entry entry = EntryParser.parse("abadžija", "портно’й, шью’щий из абы’",
                    "а“баџија$C#м$#.#портно’й$RV#,$PRV#шью’щий$RV#из$RV#абы’$RV#;#ср$#.#аба$CL#.#");

            assertThat(entry.senses().getFirst().translations())
                    .containsExactly("портно́й, шью́щий из абы́");
        }
    }

    @Nested
    @DisplayName("Примеры употребления")
    class Examples {

        @Test
        @DisplayName("тильда раскрывается в основу заглавного слова")
        void expandsTilde() {
            Entry entry = EntryParser.parse("voda", "вода’;река’", VODA);

            assertThat(entry.senses().getFirst().examples())
                    .extracting(Entry.Example::serbian)
                    .containsExactly("во̀да за пиће", "слатка во̀да");
        }

        @Test
        @DisplayName("перевод примера отличается от перевода слова по пустой помете")
        void separatesExampleTranslationFromWordTranslation() {
            Entry entry = EntryParser.parse("voda", "вода’;река’", VODA);

            Entry.Sense first = entry.senses().getFirst();
            assertThat(first.translations()).containsExactly("вода́");
            assertThat(first.examples())
                    .extracting(Entry.Example::russian)
                    .containsExactly("питьева́я вода́", "пре́сная вода́");
        }
    }

    @Nested
    @DisplayName("Устойчивые обороты")
    class Idioms {

        @Test
        @DisplayName("блок после ◊ выносится отдельно и сохраняет знаки препинания")
        void collectsIdiomsAfterMarker() {
            Entry entry = EntryParser.parse("voda", "вода’;река’", VODA);

            assertThat(entry.idioms()).hasSize(1);
            assertThat(entry.idioms().getFirst().serbian()).isEqualTo("велика во̀да");
            assertThat(entry.idioms().getFirst().russian()).isEqualTo("полово́дье, разли́в");
        }
    }

    @Nested
    @DisplayName("Омонимы в одной строке")
    class Homonyms {

        /** Настоящая статья «gora»: два омонима, разделённые разрывом абзаца. */
        private static final String GORA = """
                го‛ра$C#ж$#.#,#го“ра$C#ж$#.#I$#1$D#)#гора’$RV#;#2$D#)#лес$RV#.#@#@#\
                го‛ра$C#ж$#.#,#го“ра$C#ж$#.#II$#прост$#.#паду’чая$RV#боле’знь$RV#.#""";

        @Test
        @DisplayName("вариант написания заглавного слова не превращается в пример")
        void headwordVariantIsNotAnExample() {
            Entry entry = EntryParser.parse("gora", "гора’;лес;паду’чая боле’знь", GORA);

            assertThat(entry.senses())
                    .allSatisfy(sense -> assertThat(sense.examples()).isEmpty());
        }

        @Test
        @DisplayName("повторяющаяся помета не задваивается")
        void marksAreNotDuplicated() {
            Entry entry = EntryParser.parse("gora", "гора’;лес;паду’чая боле’знь", GORA);

            assertThat(entry.grammar()).containsExactly("ж");
        }
    }
}
