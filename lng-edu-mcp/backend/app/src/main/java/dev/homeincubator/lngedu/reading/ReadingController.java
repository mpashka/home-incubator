// @tag:vertical-slice @tag:request-id @tag:reading-block
package dev.homeincubator.lngedu.reading;

import dev.homeincubator.lngedu.common.ValidationException;
import dev.homeincubator.lngedu.reading.ReadingCommands.Comprehension;
import dev.homeincubator.lngedu.reading.ReadingCommands.GetNextChunkCommand;
import dev.homeincubator.lngedu.reading.ReadingCommands.NextChunkView;
import dev.homeincubator.lngedu.reading.ReadingCommands.RecordChunkResultCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Thin REST adapter for reading. {@code GET next} builds the reading block dynamically
 * (@tag:reading-block) and does not advance progress; {@code POST result} records the outcome and
 * advances progress, idempotent by {@code requestId} (@tag:request-id).
 */
@RestController
@RequestMapping("/api/reading")
@Tag(name = "Reading", description = "Fetch the next reading block and record its result")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping("/next")
    @Operation(summary = "Get the next reading block for a learner and book (does not advance progress)")
    public NextChunkView next(@RequestParam UUID userId, @RequestParam UUID bookId) {
        return readingService.getNextChunk(new GetNextChunkCommand(userId, bookId));
    }

    @PostMapping("/result")
    @Operation(summary = "Record a reading-block result and advance progress (idempotent by requestId)")
    public ReadingResultResponse result(@Valid @RequestBody RecordChunkResultRequest request) {
        Comprehension comprehension = parseComprehension(request.comprehension());
        RecordChunkResultCommand cmd = new RecordChunkResultCommand(
                request.userId(), request.bookId(), request.sessionId(),
                request.endOffset(), comprehension, request.requestId());
        return ReadingResultResponse.of(readingService.recordChunkResult(cmd));
    }

    private static Comprehension parseComprehension(String wire) {
        try {
            return Comprehension.fromWire(wire);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(
                    "comprehension must be one of understood/partial/unclear, got: " + wire);
        }
    }
}
