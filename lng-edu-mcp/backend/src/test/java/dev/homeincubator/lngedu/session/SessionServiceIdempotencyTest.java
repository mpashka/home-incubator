package dev.homeincubator.lngedu.session;

import dev.homeincubator.lngedu.book.BookRepository;
import dev.homeincubator.lngedu.session.SessionCommands.SessionView;
import dev.homeincubator.lngedu.session.SessionCommands.StartSessionCommand;
import dev.homeincubator.lngedu.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Idempotency of start_learning_session (@tag:request-id). Pure Mockito, no Spring/DB. */
@ExtendWith(MockitoExtension.class)
class SessionServiceIdempotencyTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-08T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private LearningSessionRepository sessionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;

    @Test
    void startingTwiceWithSameRequestId_createsOneSession() {
        SessionService service = new SessionService(sessionRepository, userRepository, bookRepository, clock);
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        String requestId = "req-123";
        StartSessionCommand cmd = new StartSessionCommand(userId, bookId, requestId);

        lenient().when(userRepository.existsById(userId)).thenReturn(true);
        lenient().when(bookRepository.existsById(bookId)).thenReturn(true);

        LearningSession saved = new LearningSession();
        saved.setId(UUID.randomUUID());
        saved.setUserId(userId);
        saved.setBookId(bookId);
        saved.setStartedAt(Instant.now(clock));
        saved.setRequestId(requestId);

        // First call: no existing row; second call: the row created by the first call.
        when(sessionRepository.findByRequestId(requestId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(saved));
        when(sessionRepository.save(any(LearningSession.class))).thenReturn(saved);

        SessionView first = service.startLearningSession(cmd);
        SessionView second = service.startLearningSession(cmd);

        assertThat(first.id()).isEqualTo(saved.getId());
        assertThat(second.id()).isEqualTo(saved.getId());
        // The effect (INSERT) is applied exactly once.
        verify(sessionRepository, times(1)).save(any(LearningSession.class));
    }
}
