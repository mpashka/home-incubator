package org.mpashka.worldehelper;

import java.util.Random;

public class Utils {
    public static String randomString(int length) {
        return new Random().ints('a', 'z' + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
