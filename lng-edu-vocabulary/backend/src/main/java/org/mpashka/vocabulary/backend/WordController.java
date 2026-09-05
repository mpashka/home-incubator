package org.mpashka.vocabulary.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST API словаря. Описание ответов — в {@code docs/specification/api.md}. Данные — из Postgres. */
@RestController
@RequestMapping("/api/words")
public class WordController {

    private static final int MAX_LIMIT = 100;

    private final PostgresDictionary dictionary;

    public WordController(PostgresDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /** Поиск по заглавному слову, по любой словоформе и по русскому переводу. */
    @GetMapping
    public SearchResult search(@RequestParam(name = "q", defaultValue = "") String query,
                               @RequestParam(name = "limit", defaultValue = "20") int limit,
                               @RequestParam(name = "alphabet", defaultValue = "current") String alphabet) {
        String mode = switch (alphabet) {
            case "separate", "any", "current" -> alphabet;
            default -> "current";
        };
        List<SearchItem> items = dictionary.search(query, Math.clamp(limit, 1, MAX_LIMIT), mode).stream()
                .map(found -> new SearchItem(found.headwordPlain(), found.headword(),
                        found.translations()))
                .toList();
        return new SearchResult(items, items.size());
    }

    /**
     * Полная статья по заглавному слову (кириллица без ударений).
     *
     * <p>Поля первого омонима лежат на верхнем уровне, а полный список — в {@code homonyms}.
     * Под одним написанием бывает несколько самостоятельных слов ({@code бити} — «быть»
     * и «бить»), и показывать только первое значило бы прятать половину словаря.
     */
    @GetMapping("/{name}")
    public ResponseEntity<WordResponse> byName(@PathVariable String name) {
        List<PostgresDictionary.WordCard> homonyms = dictionary.byHeadword(name);
        if (homonyms.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PostgresDictionary.WordCard first = homonyms.getFirst();
        return ResponseEntity.ok(new WordResponse(first.name(), first.headword(),
                first.headwordLatin(), first.grammar(), first.partOfSpeech(), first.senses(),
                first.idioms(), first.forms(), first.roots(), first.status(), first.needsLanguageReview(),
                first.reviewReason(), homonyms));
    }

    /** Ответ по слову: первый омоним на верхнем уровне плюс список всех. */
    public record WordResponse(String name, String headword, String headwordLatin,
                               List<String> grammar, String partOfSpeech,
                               List<PostgresDictionary.Sense> senses,
                               List<PostgresDictionary.Example> idioms,
                               List<PostgresDictionary.WordForm> forms,
                               List<PostgresDictionary.Root> roots, String status,
                               boolean needsLanguageReview, String reviewReason,
                               List<PostgresDictionary.WordCard> homonyms) {
    }

    /**
     * Найденное слово в списке поиска.
     *
     * @param name         заглавное слово без ударений — ключ для перехода к статье
     * @param headword     заглавное слово со знаками ударения
     * @param translations переводы
     */
    public record SearchItem(String name, String headword, List<String> translations) {
    }

    /** Ответ поиска. */
    public record SearchResult(List<SearchItem> items, int total) {
    }
}
