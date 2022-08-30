package org.test.mpashka;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTest {
    private static final Logger log = LoggerFactory.getLogger(JavaTest.class);

    @Test
    public void testDiv() {
        log.info("{}", 999/1000);
        log.info("{}", 999/1000);
        log.info("{}", 1999/1000);
        log.info("{}", 1000/1000);
        log.info("{}", 1001/1000);
    }
}
