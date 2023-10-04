package org.test.mpashka.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class RecordTest {

    @Test
    public void testRecord() {
        var r = new TestRecord(null, 1);
        log.info("Nu: {}", r);

        var r2 = new TestRecord(null, 1);
        log.info("Nu2: {}, eq: {}", r2, r2.equals(r));

    }

    private record TestRecord(String a, int b) {}
}
