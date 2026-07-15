// @tag:request-id
package dev.homeincubator.lngedu.session;

import dev.homeincubator.lngedu.book.BookRepository;
import dev.homeincubator.lngedu.common.NotFoundException;
import dev.homeincubator.lngedu.common.ValidationException;
import dev.homeincubator.lngedu.session.SessionCommands.FinishSessionCommand;
import dev.homeincubator.lngedu.session.SessionCommands.SessionView;
import dev.homeincubator.lngedu.session.SessionCommands.StartSessionCommand;
import dev.homeincubator.lngedu.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Sessions use case: start (idempotent by {@code request_id}) and finish a learning session.
 *
 * <p>Idempotency (@tag:request-id): {@code start_learning_session} looks up the existing row by
 * {@code request_id} first and returns it unchanged on a repeat; the DB UNIQUE constraint on
 * {@code learning_sessions.request_id} is the backstop for concurrent inserts.
 * {@code finish} is naturally idempotent — it only stamps {@code finished_at} once.
 */
@Service
public class SessionService {

    private final LearningSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final Clock clock;

    public SessionService(LearningSessionRepository sessionRepository,
                          UserRepository userRepository,
                          BookRepository bookRepository,
                          Clock clock) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    @Transactional
    public SessionView startLearningSession(StartSessionCommand cmd) {
        if (cmd.requestId() == null || cmd.requestId().isBlank()) {
            throw new ValidationException("requestId is required");
        }

        // Idempotent repeat: return the session already created for this request_id.
        var existing = sessionRepository.findByRequestId(cmd.requestId());
        if (existing.isPresent()) {
            return SessionView.of(existing.get());
        }

        if (!userRepository.existsById(cmd.userId())) {
            throw NotFoundException.of("user", cmd.userId());
        }
        if (!bookRepository.existsById(cmd.bookId())) {
            throw NotFoundException.of("book", cmd.bookId());
        }

        LearningSession session = new LearningSession();
        session.setUserId(cmd.userId());
        session.setBookId(cmd.bookId());
        session.setStartedAt(Instant.now(clock));
        session.setRequestId(cmd.requestId());
        try {
            return SessionView.of(sessionRepository.save(session));
        } catch (DataIntegrityViolationException race) {
            // Lost a concurrent race on the UNIQUE request_id: return the winner.
            return SessionView.of(sessionRepository.findByRequestId(cmd.requestId())
                    .orElseThrow(() -> race));
        }
    }

    /**
     * Resolve the learner (user id) that owns a session, for the transport-layer ownership guard
     * (@tag:auth). Throws {@link NotFoundException} if the session does not exist — the adapter then
     * asserts the authenticated account owns the returned learner before finishing. Kept
     * transport-agnostic (no SecurityContext) so core stays decoupled from the web.
     */
    @Transactional(readOnly = true)
    public UUID getSessionLearner(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .map(LearningSession::getUserId)
                .orElseThrow(() -> NotFoundException.of("session", sessionId));
    }

    @Transactional
    public SessionView finishLearningSession(FinishSessionCommand cmd) {
        LearningSession session = sessionRepository.findById(cmd.sessionId())
                .orElseThrow(() -> NotFoundException.of("session", cmd.sessionId()));
        if (session.getFinishedAt() == null) {
            session.setFinishedAt(Instant.now(clock));
            session = sessionRepository.save(session);
        }
        return SessionView.of(session);
    }
}
