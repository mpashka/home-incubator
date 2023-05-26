package org.test.mpashka;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeTest {

    @Test
    public void testAdd() {
        Instant now = Instant.now();
        Instant p1d = now.plus(1, ChronoUnit.DAYS);
        Instant p2d = now.plus(Period.ofDays(2));
        log.info("{} -> {}, {}", now, p1d, p2d);
    }

    @Test
    public void formatDuration() {
        for (ChronoUnit chronoUnit : ChronoUnit.values()) {
            try {
                log.info("1 {} == {}", chronoUnit, Duration.of(1, chronoUnit));
            } catch (Exception e) {
                log.info("No duration for {}: {}", chronoUnit, e.getMessage());
            }
        }
/*
1 Nanos == PT0.000000001S
1 Micros == PT0.000001S
1 Millis == PT0.001S
1 Seconds == PT1S
1 Minutes == PT1M
1 Hours == PT1H
1 HalfDays == PT12H
1 Days == PT24H
No duration for Weeks: Unit must not have an estimated duration
 */
        List.of("PT24H", "P1D", "P2D", "P3DT4H").forEach(t -> {
            try {
                log.info("text:{} == {}", t, Duration.parse(t));
            } catch (Exception e) {
                log.info("Can't parse {}: {}", t, e.getMessage());
            }
        });
    }

    @Test
    public void testParse() throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
        log.info("D: {}", fmt.parse("Fri Jan 13 09:36:03 MSK 2023"));
    }

}
