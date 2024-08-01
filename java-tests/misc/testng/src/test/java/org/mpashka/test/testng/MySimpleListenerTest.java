package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

@Slf4j
public class MySimpleListenerTest {
    @Test
    public void test() {
        log.info("::simple-test");
    }
}
