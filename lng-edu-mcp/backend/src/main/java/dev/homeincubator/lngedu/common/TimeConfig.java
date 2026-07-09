package dev.homeincubator.lngedu.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a single {@link Clock} so services read "now" through it. Time is stored in UTC;
 * user-facing day boundaries are computed in the user's timezone (see StatsService).
 * Tests inject a fixed Clock instead.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
