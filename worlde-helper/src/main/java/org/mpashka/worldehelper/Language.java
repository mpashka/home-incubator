package org.mpashka.worldehelper;

import java.util.HashMap;
import java.util.Map;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;

public record Language(String name, char firstLetter, char lastLetter, String fileName) {
    public static final Map<String, Language> languages = new HashMap<>();

    public Language {
        languages.put(name, this);
    }

    int letters() {
        return lastLetter - firstLetter + 1;
    }

    int idx(char c) {
        return c - firstLetter;
    }

    boolean isCorrect(char c) {
        return c >= firstLetter && c <= lastLetter;
    }

    boolean isCorrect(String word) {
        if (word.length() != WORD_LENGTH) return false;
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (!isCorrect(word.charAt(i))) return false;
        }
        return true;
    }

    public static final Language rus = new Language("rus", 'а', 'я', ".in/rus.txt");
    public static final Language eng = new Language("eng", 'a', 'z', ".in/eng.txt");
}
