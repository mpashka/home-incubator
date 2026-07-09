package dev.homeincubator.lngedu.stats;

import dev.homeincubator.lngedu.common.NotFoundException;
import dev.homeincubator.lngedu.reading.ReadingEventRepository;
import dev.homeincubator.lngedu.session.LearningSession;
import dev.homeincubator.lngedu.session.LearningSessionRepository;
import dev.homeincubator.lngedu.user.User;
import dev.homeincubator.lngedu.user.UserRepository;
import dev.homeincubator.lngedu.vocabulary.VocabularyItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Stats use case: daily stats for a learner. Timestamps are stored in UTC; the "today" window is
 * computed in the learner's timezone ({@code users.timezone}) and converted back to UTC instants.
 */
@Service
public class StatsService {

    private final UserRepository userRepository;
    private final LearningSessionRepository sessionRepository;
    private final VocabularyItemRepository vocabularyItemRepository;
    private final ReadingEventRepository readingEventRepository;
    private final Clock clock;

    public StatsService(UserRepository userRepository,
                        LearningSessionRepository sessionRepository,
                        VocabularyItemRepository vocabularyItemRepository,
                        ReadingEventRepository readingEventRepository,
                        Clock clock) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.vocabularyItemRepository = vocabularyItemRepository;
        this.readingEventRepository = readingEventRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DailyStatsView getDailyStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("user", userId));

        ZoneId zone = ZoneId.of(user.getTimezone());
        Instant now = Instant.now(clock);
        LocalDate today = now.atZone(zone).toLocalDate();
        Instant dayStart = today.atStartOfDay(zone).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant();

        List<LearningSession> sessions =
                sessionRepository.findByUserIdAndStartedAtBetween(userId, dayStart, dayEnd);

        long minutes = sessions.stream()
                .mapToLong(s -> {
                    Instant end = s.getFinishedAt() != null ? s.getFinishedAt() : now;
                    long seconds = Duration.between(s.getStartedAt(), end).getSeconds();
                    return Math.max(0, seconds) / 60;
                })
                .sum();

        long newWords = vocabularyItemRepository
                .countByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, dayStart, dayEnd);

        long charsRead = readingEventRepository.sumCharsRead(userId, dayStart, dayEnd);
        long blocksRead = readingEventRepository
                .countByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(userId, dayStart, dayEnd);

        return new DailyStatsView(today, user.getTimezone(), minutes, sessions.size(),
                newWords, charsRead, blocksRead);
    }
}
