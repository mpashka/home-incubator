package org.mpashka.totemftc.api;

import javax.enterprise.context.ApplicationScoped;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Utils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String TIME_FORMAT = "HH:mm";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    private static final char[] RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final Random random = new Random();
    private static final char[] HEX_ARRAY_UPPERCASE = "0123456789ABCDEF".toCharArray();
    private static final char[] HEX_ARRAY_LOWERCASE = "0123456789abcdef".toCharArray();

    public static String generateRandomString(int length) {
        return random.ints(length, 0, RANDOM_CHARS.length)
                .collect(() -> new StringBuilder(length), (s, i) -> s.append(RANDOM_CHARS[i]), StringBuilder::append)
                .toString();
    }

    public static String bytesToHex(byte[] bytes, boolean uppercase) {
        char[] hexArray = uppercase ? HEX_ARRAY_UPPERCASE : HEX_ARRAY_LOWERCASE;
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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
        if (phone == null) {
            return null;
        }
        phone = phone.replaceAll("[\\D]", "");
        if (phone.charAt(0) == '8') {
            phone = "7" + phone.substring(1);
        }
        return phone;
    }

}
