package org.test.mpashka.core;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TimeTest {
    @Test
    public void testDuration() {
        Duration d3m = Duration.of(3, ChronoUnit.MINUTES);
        log.info("3M: {}", d3m);
        Duration d3m_1 = Duration.parse("PT3M");
        log.info("3M_: {}", d3m_1);
    }
}
