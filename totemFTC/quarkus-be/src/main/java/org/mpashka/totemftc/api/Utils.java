package org.mpashka.totemftc.api;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Utils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String TIME_FORMAT = "HH:mm";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter JSON_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


    public static final char[] RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    /**
     * https://developers.cloudflare.com/rules/transform/request-header-modification/reference/header-format
     * https://stackoverflow.com/questions/47687379/what-characters-are-allowed-in-http-header-values
     */
    public static final char[] HTTP_VALUE_RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_:;.,\\/\"'?!(){}[]@<>=-+*#$&`|~^%".toCharArray();
    private static final Random random = new Random();
    private static final char[] HEX_ARRAY_UPPERCASE = "0123456789ABCDEF".toCharArray();
    private static final char[] HEX_ARRAY_LOWERCASE = "0123456789abcdef".toCharArray();

    public static String generateRandomString(int length, char[] randomChars) {
        return random.ints(length, 0, randomChars.length)
                .collect(() -> new StringBuilder(length), (s, i) -> s.append(randomChars[i]), StringBuilder::append)
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

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean notBlank(String str) {
        return str != null && !str.isBlank();
    }

    public static String firstNonBlank(String... strings) {
        for (String string : strings) {
            if (notBlank(string)) {
                return string;
            }
        }
        return null;
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
