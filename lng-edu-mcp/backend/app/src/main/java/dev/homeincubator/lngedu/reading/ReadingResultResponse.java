package dev.homeincubator.lngedu.reading;

import dev.homeincubator.lngedu.reading.ReadingCommands.ChunkResultView;

import java.util.UUID;

/**
 * Web response for a recorded reading result. Exposes the comprehension as its stable wire code
 * (understood/partial/unclear) rather than the enum constant name.
 */
public record ReadingResultResponse(UUID bookId, int positionChar, String comprehension) {

    static ReadingResultResponse of(ChunkResultView view) {
        return new ReadingResultResponse(view.bookId(), view.positionChar(), view.result().wireCode());
    }
}
