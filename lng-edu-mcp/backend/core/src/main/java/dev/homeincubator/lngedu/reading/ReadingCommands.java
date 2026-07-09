// @tag:reading-block
package dev.homeincubator.lngedu.reading;

import java.util.UUID;

/** Typed command/result records for the reading use case. */
public final class ReadingCommands {

    private ReadingCommands() {
    }

    /**
     * Comprehension outcome for a delivered block (понятно / частично / непонятно). The wire code
     * is the value persisted in {@code reading_events.comprehension}.
     */
    public enum Comprehension {
        UNDERSTOOD("understood"),
        PARTIAL("partial"),
        NOT_UNDERSTOOD("unclear");

        private final String wireCode;

        Comprehension(String wireCode) {
            this.wireCode = wireCode;
        }

        public String wireCode() {
            return wireCode;
        }

        public static Comprehension fromWire(String code) {
            for (Comprehension c : values()) {
                if (c.wireCode.equals(code)) {
                    return c;
                }
            }
            throw new IllegalArgumentException("unknown comprehension code: " + code);
        }
    }

    public record GetNextChunkCommand(UUID userId, UUID bookId) {
    }

    /** The dynamically assembled reading block. Reading is a query: progress is NOT advanced. */
    public record NextChunkView(
            UUID bookId,
            String language,
            int startOffset,
            int endOffset,
            String text,
            boolean endOfBook) {
    }

    public record RecordChunkResultCommand(
            UUID userId,
            UUID bookId,
            UUID sessionId,
            int endOffset,
            Comprehension result,
            String requestId) {
    }

    public record ChunkResultView(
            UUID bookId,
            int positionChar,
            Comprehension result) {
    }
}
