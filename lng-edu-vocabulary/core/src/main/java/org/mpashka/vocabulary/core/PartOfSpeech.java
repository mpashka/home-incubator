package org.mpashka.vocabulary.core;

/** Часть речи. */
// @tag:part-of-speech
public enum PartOfSpeech {

    NOUN("существительное"),
    VERB("глагол"),
    ADJECTIVE("прилагательное"),
    ADVERB("наречие"),
    PRONOUN("местоимение"),
    NUMERAL("числительное"),
    INTERJECTION("междометие"),
    CONJUNCTION("союз"),
    PREPOSITION("предлог"),
    PARTICLE("частица"),

    /** Определить не удалось — слово уходит в очередь на разбор языковой моделью. */
    UNKNOWN("не определена");

    private final String title;

    PartOfSpeech(String title) {
        this.title = title;
    }

    /** Название по-русски. */
    public String title() {
        return title;
    }
}
