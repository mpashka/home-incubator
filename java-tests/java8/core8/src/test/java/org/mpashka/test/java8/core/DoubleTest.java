package org.mpashka.test.java8.core;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class DoubleTest {
    private static final Comparator<String> c = Comparator.comparingDouble(Double::parseDouble);
    public static final double D = 0.0001;

    @Test
    public void test() {
        test("1", "1.000");
        double d = 0;
        for (int i = 0; i < 10000; i++) {
            String s1 = String.valueOf(d);
            test(s1, s1);
            d += D;
            test(s1, String.valueOf(d - D));
            test(s1, String.valueOf(i * D));
        }
    }

    private void test(String s1, String s2) {
        boolean d = Double.parseDouble(s1) == Double.parseDouble(s2);
        int compare = c.compare("1", "1.000");
        if (compare != 0) {
//        if (!d || compare != 0) {
            log.info("{} == {}: {}, {}", s1, s2, d, compare);
        }
    }
}
