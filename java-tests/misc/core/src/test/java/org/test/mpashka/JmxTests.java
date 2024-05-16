package org.test.mpashka;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JmxTests {

    @Test
    public void test() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        log.info("Runtime: {}", runtimeMXBean);
    }
}
