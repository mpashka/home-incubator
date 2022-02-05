package org.mpashka.worldehelper;

import java.util.HashMap;
import java.util.Map;

public record Language(String name, char firstLetter, char lastLetter, String fileName) {
    public static final Map<String, Language> languages = new HashMap<>();

    public Language {
        languages.put(name, this);
    }

    public static final Language rus = new Language("rus", 'а', 'я', ".in/rus.txt");
    public static final Language eng = new Language("eng", 'a', 'z', ".in/eng.txt");
}
