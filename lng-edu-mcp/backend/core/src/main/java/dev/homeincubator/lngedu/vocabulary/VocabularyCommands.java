package dev.homeincubator.lngedu.vocabulary;

import java.time.Instant;
import java.util.UUID;

/** Typed command/result records for the vocabulary use case. */
public final class VocabularyCommands {

    private VocabularyCommands() {
    }

    public record RecordUnknownWordCommand(
            UUID userId,
            String language,
            String lemma,
            String context,
            UUID sessionId,
            String requestId) {
    }

    public record VocabularyItemView(
            UUID id,
            String language,
            String lemma,
            String status,
            String lastContext,
            Instant lastSeenAt) {

        static VocabularyItemView of(VocabularyItem item) {
            return new VocabularyItemView(item.getId(), item.getLanguage(), item.getLemma(),
                    item.getStatus(), item.getLastContext(), item.getLastSeenAt());
        }
    }
}
