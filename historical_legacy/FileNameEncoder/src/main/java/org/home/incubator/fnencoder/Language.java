package org.home.incubator.fnencoder;

/**
 * Created by IntelliJ IDEA.
 * User: moukhataevs
 * Time: 17:26:46
 * To change this template use File | Settings | File Templates.
 */
public enum Language {
    English('e', Character.UnicodeBlock.BASIC_LATIN), Russian('r', Character.UnicodeBlock.CYRILLIC);

    private char indicator;
    private Character.UnicodeBlock charBlock;

    Language(char indicator, Character.UnicodeBlock charBlock) {
        this.indicator = indicator;
        this.charBlock = charBlock;
    }

    public char getIndicator() {
        return indicator;
    }

    public static final String SPECIAL_CHARS = " `~!@#$%^&*()_+=-{}|\\[];':\",./<>?";

    public static Language getLanguage(char c) {
        if (SPECIAL_CHARS.indexOf(c) != -1) return null;
        for (Language language : values()) {
            if (Character.UnicodeBlock.of(c) == language.charBlock) {
                return language;
            }
        }
        return null;
    }
}
