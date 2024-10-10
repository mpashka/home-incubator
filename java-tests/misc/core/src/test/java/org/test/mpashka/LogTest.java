package org.test.mpashka;

import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.test.mpashka.LogTest.LambdaVal.lambdaVal;

@Slf4j
public class LogTest {
    @Test
    public void testLog() {
        Supplier<String> val = () -> "World";
        log.info("Hello, {}!", val);
        log.info("Hello val, {}!", lambdaVal(() -> "World"));
    }

    static class LambdaVal {
        private final ToStringSupplier val;

        public LambdaVal(ToStringSupplier val) {
            this.val = val;
        }

        public static LambdaVal lambdaVal(ToStringSupplier val) {
            return new LambdaVal(val);
        }

        @Override
        public String toString() {
            return val.val();
        }
    }

    @FunctionalInterface
    interface ToStringSupplier {
        String val();
    }
}
