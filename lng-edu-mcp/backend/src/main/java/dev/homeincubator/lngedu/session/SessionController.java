// @tag:vertical-slice @tag:request-id
package dev.homeincubator.lngedu.session;

import dev.homeincubator.lngedu.session.SessionCommands.FinishSessionCommand;
import dev.homeincubator.lngedu.session.SessionCommands.SessionView;
import dev.homeincubator.lngedu.session.SessionCommands.StartSessionCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Thin REST adapter for learning sessions. Delegates to {@link SessionService}; start is idempotent
 * by {@code requestId} (@tag:request-id).
 */
@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "Start and finish learning sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @Operation(summary = "Start a learning session (idempotent by requestId)")
    public SessionView start(@Valid @RequestBody StartSessionRequest request) {
        return sessionService.startLearningSession(
                new StartSessionCommand(request.userId(), request.bookId(), request.requestId()));
    }

    @PostMapping("/{sessionId}/finish")
    @Operation(summary = "Finish a learning session (idempotent: stamps finishedAt once)")
    public SessionView finish(@PathVariable UUID sessionId) {
        return sessionService.finishLearningSession(new FinishSessionCommand(sessionId));
    }
}
