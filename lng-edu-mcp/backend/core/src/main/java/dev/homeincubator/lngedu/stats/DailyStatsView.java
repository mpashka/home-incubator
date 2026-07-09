package dev.homeincubator.lngedu.stats;

import java.time.LocalDate;

/**
 * Daily learning stats for a learner, computed for "today" in the learner's timezone.
 *
 * <p>{@code charsRead} and {@code blocksRead} are aggregated from {@code reading_events} recorded
 * within the day window.
 */
public record DailyStatsView(
        LocalDate day,
        String timezone,
        long minutesStudied,
        long sessions,
        long newWords,
        long charsRead,
        long blocksRead) {
}
