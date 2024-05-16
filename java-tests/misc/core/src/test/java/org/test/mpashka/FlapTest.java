package org.test.mpashka;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Log4j2
public class FlapTest {

    private static int iteration = 0;

    @Test
    public void test() {
        boolean fail = iteration == 0 || (5 - iteration) * Math.random() > 1;
        log.info("Test fail: {}, iteration: {}, pid: {}", fail, iteration, ProcessHandle.current().pid());
        iteration++;
        assertThat(fail, is(false));
    }
}
