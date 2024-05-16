package org.test.mpashka;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Log4j2
public class PidTest {
    @Test
    public void test() {
        log.info("Test pid: {}", ProcessHandle.current().pid());
    }
}
