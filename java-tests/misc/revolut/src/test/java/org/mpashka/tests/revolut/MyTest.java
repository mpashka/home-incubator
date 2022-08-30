package org.mpashka.tests.revolut;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTest {

    private static final Logger log = LoggerFactory.getLogger(MyTest.class);

    @Test
    public void testLogs() {
        log.info("Info");
        log.debug("Debug");
        log.trace("Trace");
    }
}
