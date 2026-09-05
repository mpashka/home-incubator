package org.mpashka.vocabulary.backend;

import org.mpashka.vocabulary.core.Form;
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

    /**
     * Поиск по заглавному слову, по русскому переводу и — при {@code forms=true} —
     * по любой словоформе.
     *
     * <p>У найденного по форме слова в ответе стоит {@code matchedForm}: сама форма и её
     * грамматическая помета. Без этого непонятно, почему в ответ на «вода» пришло
     * {@code во̏д}.
     */
    @GetMapping
    public SearchResult search(@RequestParam(name = "q", defaultValue = "") String query,
                               @RequestParam(name = "limit", defaultValue = "20") int limit,
                               @RequestParam(name = "alphabet", defaultValue = "current") String alphabet,
                               @RequestParam(name = "forms", defaultValue = "true") boolean forms) {
        String mode = switch (alphabet) {
            case "separate", "any", "current" -> alphabet;
            default -> "current";
        };
        List<SearchItem> items = dictionary.search(query, Math.clamp(limit, 1, MAX_LIMIT), mode, forms).stream()
                .map(found -> new SearchItem(found.id(), found.headwordPlain(), found.headword(),
                        found.translations(), found.matchedForm()))
                .toList();
        return new SearchResult(items, items.size());
    }

    /**
     * Статья по идентификатору слова: переход из списка найденного и из карточки.
     *
     * <p>Нужен именно он, а не написание: по написанию «вода» словарь имеет право найти
     * и {@code во̏д}, у которого это родительный падеж, а щелчок по строке списка должен
     * приводить ровно в то слово, по которому щёлкнули.
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<WordResponse> byId(@PathVariable long id) {
        return respond(dictionary.lookupById(id));
    }

    /**
     * Полная статья по написанию слова (кириллица без ударений).
     *
     * <p>Сначала ищется слово с таким написанием: в списке тогда стоят его омонимы —
     * самостоятельные слова одного написания ({@code бити} — «быть» и «бить»). Только
     * если такого слова нет, включается поиск по словоформе, и тогда в списке — разные
     * слова, у каждого своя {@code matchedForm}. Что именно произошло, говорит
     * {@code matchedBy}.
     */
    @GetMapping("/{name}")
    public ResponseEntity<WordResponse> byName(@PathVariable String name) {
        return respond(dictionary.lookupByName(name));
    }

    private ResponseEntity<WordResponse> respond(PostgresDictionary.Lookup lookup) {
        if (lookup.words().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new WordResponse(lookup.matchedBy(), lookup.words()));
    }

    /**
     * Ответ по слову.
     *
     * @param matchedBy как нашли: {@code headword} — по написанию, {@code form} — по
     *                  словоформе, {@code id} — переход по конкретному слову
     * @param words     карточки: при {@code headword} и {@code id} — омонимы одного
     *                  написания, при {@code form} — разные слова, совпавшие формой
     */
    public record WordResponse(String matchedBy, List<PostgresDictionary.WordCard> words) {
    }

    /**
     * Найденное слово в списке поиска.
     *
     * @param id           идентификатор слова — по нему открывается ровно эта статья
     * @param name         заглавное слово без ударений
     * @param headword     заглавное слово со знаками ударения
     * @param translations переводы
     * @param matchedForm  форма, по которой слово нашлось, и её помета; {@code null},
     *                     когда совпало само заглавное слово или перевод
     */
    public record SearchItem(long id, String name, String headword, List<String> translations,
                             Form matchedForm) {
    }

    /** Ответ поиска. */
    public record SearchResult(List<SearchItem> items, int total) {
    }
}
