package org.test.mpashka;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTest {
    private static final Logger log = LoggerFactory.getLogger(JavaTest.class);

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
    public void testProperties() throws IOException {
        Properties properties = new Properties();
        properties.put("uddi:gosuslugi.ru:services:internal/geps-file", "http://test.gosuslugi.org/");
        StringWriter writer = new StringWriter();
        properties.store(writer, null);
        log.info("Properties: {}", writer.getBuffer());
    }

    @Test
    public void testVarArgs() {
        varArgs();
    }

    private void varArgs(String... args) {
        log.info("Args: {}", (Object) args);
    }
}
