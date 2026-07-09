package dev.homeincubator.lngedu.stats;

import dev.homeincubator.lngedu.reading.ReadingEventRepository;
import dev.homeincubator.lngedu.session.LearningSession;
import dev.homeincubator.lngedu.session.LearningSessionRepository;
import dev.homeincubator.lngedu.user.User;
import dev.homeincubator.lngedu.user.UserRepository;
import dev.homeincubator.lngedu.vocabulary.VocabularyItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/** Daily stats aggregate chars/blocks from reading_events in the user's tz. Pure Mockito. */
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    // 2026-07-08T02:00Z is still 2026-07-07 in a UTC-05:00 zone -> exercises tz day boundary.
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-08T02:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;
    @Mock
    private LearningSessionRepository sessionRepository;
    @Mock
    private VocabularyItemRepository vocabularyItemRepository;
    @Mock
    private ReadingEventRepository readingEventRepository;

    // User / LearningSession have protected constructors; anonymous subclasses can call them.
    private static User user(String tz) {
        User u = new User() {
        };
        u.setTimezone(tz);
        return u;
    }

    private static LearningSession session(Instant started, Instant finished) {
        LearningSession s = new LearningSession() {
        };
        s.setStartedAt(started);
        s.setFinishedAt(finished);
        return s;
    }

    @Test
    void aggregatesCharsAndBlocksFromReadingEventsInUserTimezone() {
        UUID userId = UUID.randomUUID();
        StatsService service = new StatsService(userRepository, sessionRepository,
                vocabularyItemRepository, readingEventRepository, clock);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user("America/New_York")));

        LearningSession s = session(
                Instant.parse("2026-07-07T20:00:00Z"), Instant.parse("2026-07-07T20:30:00Z"));
        when(sessionRepository.findByUserIdAndStartedAtBetween(eq(userId), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(s));
        when(vocabularyItemRepository
                .countByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(eq(userId), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(4L);
        when(readingEventRepository.sumCharsRead(eq(userId), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(850L);
        when(readingEventRepository
                .countByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(eq(userId), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(6L);

        DailyStatsView view = service.getDailyStats(userId);

        assertThat(view.charsRead()).isEqualTo(850);
        assertThat(view.blocksRead()).isEqualTo(6);
        assertThat(view.newWords()).isEqualTo(4);
        assertThat(view.minutesStudied()).isEqualTo(30);
        assertThat(view.sessions()).isEqualTo(1);
        assertThat(view.timezone()).isEqualTo("America/New_York");
        // "Today" in New York (UTC-04:00 in July) at 2026-07-08T02:00Z is still 2026-07-07.
        assertThat(view.day()).isEqualTo(java.time.LocalDate.of(2026, 7, 7));

        // The reading-event window is the NY-local day converted to UTC instants:
        // 2026-07-07T00:00-04:00 == 2026-07-07T04:00Z .. 2026-07-08T04:00Z.
        ArgumentCaptor<Instant> from = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> to = ArgumentCaptor.forClass(Instant.class);
        org.mockito.Mockito.verify(readingEventRepository).sumCharsRead(eq(userId), from.capture(), to.capture());
        assertThat(from.getValue()).isEqualTo(Instant.parse("2026-07-07T04:00:00Z"));
        assertThat(to.getValue()).isEqualTo(Instant.parse("2026-07-08T04:00:00Z"));
    }
}
