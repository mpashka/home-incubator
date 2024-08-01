package org.mpashka.test.java8.core;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Regex8Test {
    private static final Logger log = LoggerFactory.getLogger(Regex8Test.class);

    @Test
    public void testRegexp() {
        Pattern NON_ASCII = Pattern.compile("[^\\p{Alnum}.\\-_]+");

        log.info("{}", NON_ASCII.matcher("my-msg-name  . ,(:)['] ").replaceAll("_"));
        log.info("{}", NON_ASCII.matcher("my-msg-name ,(:)['] ").replaceAll("_"));
        log.info("{}", Pattern.compile("qwefq/(?<podId>[^/]+)/").matcher("my-ms/g-name ,(:)['] ").replaceAll("_podId:${podId}_"));
    }
}
