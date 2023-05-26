package org.test.mpashka;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTest {
    private static final Logger log = LoggerFactory.getLogger(JavaTest.class);

    public static class MyThrowable extends Throwable {}
    public void myMethod() throws MyThrowable {
        throw new MyThrowable();
    }

    @Test
    public void testJavaAdd() {
        String a = "1", b = "1", c = a + b;
        log.info("{} + {} = {}", a, b, c);
    }

    @Test
    public void testPP() {
        int i = 5;
        i = ++i + ++i;
        log.info("++I = {}", i);

        i = 5;
        i = ++i + ++i + ++i;
        log.info("++I3 = {}", i);

        i = 5;
        i = i++ + i++;
        log.info("I++ = {}", i);

        i = 5;
        i = i++ + i++ + i++;
        log.info("I++3 = {}", i);
    }

    @Test
    public void testDiv() {
        log.info("{}", 999/1000);
        log.info("{}", 999/1000);
        log.info("{}", 1999/1000);
        log.info("{}", 1000/1000);
        log.info("{}", 1001/1000);
    }

    @Test
    public void uuid() {
        log.info("UUID: {}", UUID.randomUUID());
    }

    @Test
    public void testPropertiesSave() throws IOException {
        Properties properties = new Properties();
        properties.put("uddi:gosuslugi.ru:services:internal/geps-file", "http://test.gosuslugi.org/");
        StringWriter writer = new StringWriter();
        properties.store(writer, null);
        log.info("Properties: {}", writer.getBuffer());
    }

    @Test
    public void testPropertiesLoad() throws IOException {
        Properties properties = new Properties();
        StringReader in = new StringReader("uddi\\:gosuslugi.ru\\:services\\:internal/geps-file=http://test.gosuslugi.org/");
        properties.load(in);
        log.info("Properties: {}", properties);
    }

    @Test
    public void testParseToStream() {
        String path = "/aaa/bbb/ccc";
        int max = 10;
        Pattern pattern = Pattern.compile("/");
        Matcher matcher = pattern.matcher(path);
        String collect = pattern.splitAsStream(path).filter(n -> !n.isBlank()).limit(max)
                .collect(Collectors.joining());
        log.info("Collect: {}", collect);
    }

    @Test
    public void testPlus() {
        int i = 3;
        int j = i++ + i++ + i++;
        log.info("i:{}, j:{}", i, j);

        Function<int[], int[]> p = in -> {
            int result = in[0];
            in[0] += 1;
            return new int[]{result};
        };

        int[] i1 = {3};
        int j1 = p.apply(i1)[0] + p.apply(i1)[0] + p.apply(i1)[0];
        log.info("i1:{}, j1:{}", i1, j1);
    }

    @Test
    public void testVarArgs() {
        varArgs();
    }

    private void varArgs(String... args) {
        log.info("Args: {}", (Object) args);
    }

    @Test
    public void testUUID() {
        long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
//        String uuid = "a6d9e290-39e3-11ed-919b-ebfdc5cb71e0";
//        String uuid = "f01f3ff2-39ef-11ed-b210-ba4d25e9a5f6";
        String uuid = "a66c7acc-39e3-11ed-a099-0050569b9918";
        long time = (UUID.fromString(uuid).timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
        ZonedDateTime instant = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault());
        log.info("Time: {} = {}", uuid, instant);
    }

    @Test
    public void testSysArgs() {
        String key = "uddi:gosuslugi.ru:services:internal/smeva-api";
        String val = System.getProperty(key);
        log.info("{} = {}", key, val);
    }

    @Test
    public void testMinus() {
        int i = Integer.MIN_VALUE;
        log.info("-i: {}", -i);
    }

    @Test
    public void testSplit() {
        String token = "eyJ0eXAiOiJKV1QifQ==.eyJ1c2VyT2lkIjoxMDIwNywic2NvcGUiOiJodHRwOi8vZXNpYS5nb3N1c2x1Z2kucnUvdXNyX2luZj9vaWQ9MTAyMDciLCJpc3MiOiJ0ZXN0LWlzc3VlciIsInVybjplc2lhOnNpZCI6InRlc3Qtc2lkIiwidXJuOmVzaWE6c2JqX2lkIjozLCJleHAiOjEsImlhdCI6MiwiY2xpZW50X2lkIjoidGVzdC1jbGllbnQifQ==.sign";
        String[] tokenParts = token.split("\\.", 3);
        log.info("L: {}", tokenParts.length);
    }
}
