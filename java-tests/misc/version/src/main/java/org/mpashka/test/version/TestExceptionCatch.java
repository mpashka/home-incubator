package org.mpashka.test.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

//@Slf4j
public class TestExceptionCatch {
    private static final Logger log = LoggerFactory.getLogger(TestExceptionCatch.class);

    public void testMethod() {
        try {
            otherMethod();
        } catch (Exception e) {
            log.info("aaa", e);
            // Works starting from java 7
            throw e;
        }
    }

    public void otherMethod() throws RuntimeException {

    }
}
