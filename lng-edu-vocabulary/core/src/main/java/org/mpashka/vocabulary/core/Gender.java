package org.mpashka.vocabulary.core;

import java.util.List;

/** Род существительного. */
// @tag:word-forms
public enum Gender {

    MASCULINE("мужской"),
    FEMININE("женский"),
    NEUTER("средний");

    private final String title;

    Gender(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    /** Род по грамматическим пометам статьи либо {@code null}, если рода среди них нет. */
    public static Gender fromMarks(List<String> marks) {
        for (String mark : marks) {
            switch (mark) {
                case "м" -> {
                    return MASCULINE;
                }
                case "ж" -> {
                    return FEMININE;
                }
                case "с" -> {
                    return NEUTER;
                }
                default -> {
                    // помета не о роде — смотрим дальше
                }
            }
        }
        return null;
    }
}
