package dev.homeincubator.lngedu.session;

import java.time.Instant;
import java.util.UUID;

/** Typed command/result records for the sessions use case. */
public final class SessionCommands {

    private SessionCommands() {
    }

    public record StartSessionCommand(UUID userId, UUID bookId, String requestId) {
    }

    public record FinishSessionCommand(UUID sessionId) {
    }

    public record SessionView(
            UUID id,
            UUID userId,
            UUID bookId,
            Instant startedAt,
            Instant finishedAt,
            boolean active) {

        static SessionView of(LearningSession s) {
            return new SessionView(s.getId(), s.getUserId(), s.getBookId(),
                    s.getStartedAt(), s.getFinishedAt(), s.getFinishedAt() == null);
        }
    }
}
