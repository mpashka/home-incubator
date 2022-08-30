package org.test.mpashka;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public class TimeTest {
    private static final Logger log = LoggerFactory.getLogger(TimeTest.class);

    @Test
    public void testAdd() {
        Instant now = Instant.now();
        Instant p1d = now.plus(1, ChronoUnit.DAYS);
        Instant p2d = now.plus(Period.ofDays(2));
        log.info("{} -> {}, {}", now, p1d, p2d);
    }


}
