package dev.homeincubator.lngedu.vocabulary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Web request body for recording an unknown word. Validated before reaching {@link VocabularyService}. */
public record RecordUnknownWordRequest(
        @NotNull UUID userId,
        @NotBlank String language,
        @NotBlank String lemma,
        String context,
        UUID sessionId,
        @NotBlank String requestId) {
}
