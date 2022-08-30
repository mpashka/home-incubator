package org.test.mpashka;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexpTest {

    private static final Logger log = LoggerFactory.getLogger(RegexpTest.class);

    @Test
    public void testRegexp() {
        Pattern NON_ASCII = Pattern.compile("[^\\p{Alnum}.\\-_]+");

        log.info("{}", NON_ASCII.matcher("my-msg-name  . ,(:)['] ").replaceAll("_"));
        log.info("{}", NON_ASCII.matcher("my-msg-name ,(:)['] ").replaceAll("_"));
    }
}
