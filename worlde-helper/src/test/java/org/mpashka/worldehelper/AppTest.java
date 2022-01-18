package org.mpashka.worldehelper;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void checkChar() {
        char b = 'b';
        App app = new App(new String[0]);
        log.info("{} / {} / {}:{}", (int) b, b - app.firstLetter, app.firstLetter, app.letters);
    }
}
