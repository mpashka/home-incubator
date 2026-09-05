package org.mpashka.vocabulary.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Точка входа бэкенда словаря. */
@SpringBootApplication
public class VocabularyApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabularyApplication.class, args);
    }
}
