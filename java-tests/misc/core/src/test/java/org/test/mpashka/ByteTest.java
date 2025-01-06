package org.test.mpashka;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ByteTest {
    @Test
    public void testByte() {
        printHex((byte) 1);
        printHex((byte) 100);
        printHex((byte) 200);
    }

    private static void printHex(byte a) {
        log.info("i = {}. Hex:{}/S16:{}/print:{}'", a, Integer.toHexString(a), Integer.toString(a, 16),
                String.format("%02X", a)
        );
    }
}
