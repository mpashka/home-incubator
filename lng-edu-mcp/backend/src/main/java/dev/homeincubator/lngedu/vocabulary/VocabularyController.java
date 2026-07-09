// @tag:vertical-slice @tag:request-id
package dev.homeincubator.lngedu.vocabulary;

import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.RecordUnknownWordCommand;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.VocabularyItemView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Thin REST adapter for vocabulary. Delegates to {@link VocabularyService}; recording is idempotent
 * by {@code requestId} (@tag:request-id).
 */
@RestController
@RequestMapping("/api/vocabulary")
@Tag(name = "Vocabulary", description = "Record unknown words and list a learner's vocabulary")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @PostMapping
    @Operation(summary = "Record an unknown word (idempotent by requestId)")
    public VocabularyItemView recordUnknownWord(@Valid @RequestBody RecordUnknownWordRequest request) {
        return vocabularyService.recordUnknownWord(new RecordUnknownWordCommand(
                request.userId(), request.language(), request.lemma(),
                request.context(), request.sessionId(), request.requestId()));
    }

    @GetMapping
    @Operation(summary = "List a learner's vocabulary for a language")
    public List<VocabularyItemView> list(@RequestParam UUID userId, @RequestParam String language) {
        return vocabularyService.listVocabulary(userId, language);
    }
}
