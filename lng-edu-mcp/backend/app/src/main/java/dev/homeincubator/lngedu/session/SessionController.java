// @tag:vertical-slice @tag:request-id @tag:auth
package dev.homeincubator.lngedu.session;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.security.CurrentAccount;
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
 * by {@code requestId} (@tag:request-id). Both endpoints enforce that the authenticated account owns
 * the learner (@tag:auth): start guards the body's {@code userId}; finish resolves the session's
 * learner (its {@code user_id}) and guards that, so account A cannot finish account B's session.
 */
@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "Start and finish learning sessions")
public class SessionController {

    private final SessionService sessionService;
    private final AccountService accountService;
    private final CurrentAccount currentAccount;

    public SessionController(SessionService sessionService, AccountService accountService,
                             CurrentAccount currentAccount) {
        this.sessionService = sessionService;
        this.accountService = accountService;
        this.currentAccount = currentAccount;
    }

    @PostMapping
    @Operation(summary = "Start a learning session (idempotent by requestId)")
    public SessionView start(@Valid @RequestBody StartSessionRequest request) {
        accountService.assertOwnsLearner(currentAccount.accountId(), request.userId());
        return sessionService.startLearningSession(
                new StartSessionCommand(request.userId(), request.bookId(), request.requestId()));
    }

    @PostMapping("/{sessionId}/finish")
    @Operation(summary = "Finish a learning session (idempotent: stamps finishedAt once)")
    public SessionView finish(@PathVariable UUID sessionId) {
        // Resolve the session's learner (404 if the session is unknown) and assert ownership before
        // finishing, so the account can only finish its own learners' sessions (@tag:auth).
        UUID learnerId = sessionService.getSessionLearner(sessionId);
        accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        return sessionService.finishLearningSession(new FinishSessionCommand(sessionId));
    }
}
