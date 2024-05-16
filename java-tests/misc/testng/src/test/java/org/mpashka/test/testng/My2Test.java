package org.mpashka.test.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Slf4j
@Listeners({TestListener.class})
public class My2Test {

    @Test
    public void test2() {
        log.info("Must be ok2");
    }
}
