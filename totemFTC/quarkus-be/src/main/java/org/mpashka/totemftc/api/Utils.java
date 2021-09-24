package org.mpashka.totemftc.api;

import javax.enterprise.context.ApplicationScoped;
import java.util.Random;

@ApplicationScoped
public class Utils {

    private final char[] RANDOM_CHARS = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
    private final Random random = new Random();

    public String generateRandomString(int length) {
        return random.ints(length, 0, RANDOM_CHARS.length)
                .collect(() -> new StringBuilder(length), (s, i) -> s.append(RANDOM_CHARS[i]), StringBuilder::append)
                .toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean notEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Must return '+70123456789':
     *   replace 8 -> +7
     *   remove all spaces, brackets
     *
     * @param phone
     * @return
     */
    public static String normalizePhone(String phone) {
        phone = phone.replaceAll("[\\D]", "");
        if (phone.charAt(0) == '8') {
            phone = "7" + phone.substring(1);
        }
        return phone;
    }

}
