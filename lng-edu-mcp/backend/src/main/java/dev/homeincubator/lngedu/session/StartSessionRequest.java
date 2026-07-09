package dev.homeincubator.lngedu.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Web request body for starting a learning session. Validated before reaching {@link SessionService}. */
public record StartSessionRequest(
        @NotNull UUID userId,
        @NotNull UUID bookId,
        @NotBlank String requestId) {
}
