package dev.homeincubator.lngedu.reading;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/**
 * Web request body for recording a reading-block result. {@code comprehension} is the wire code
 * (understood/partial/unclear). Validated before reaching {@link ReadingService}.
 */
public record RecordChunkResultRequest(
        @NotNull UUID userId,
        @NotNull UUID bookId,
        UUID sessionId,
        @PositiveOrZero int endOffset,
        @NotBlank String comprehension,
        @NotBlank String requestId) {
}
