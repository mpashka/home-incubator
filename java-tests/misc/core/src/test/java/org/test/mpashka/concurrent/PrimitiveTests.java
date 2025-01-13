package org.test.mpashka.concurrent;

import java.util.concurrent.Phaser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class PrimitiveTests {

    @Test
    public void testPhaser() {
        Phaser phaser = new Phaser(2);
        new Thread(() -> {
            int i = 0;
            while (true) {
                try {
                    Thread.sleep(400L);
                    log.info("Arrive [{}]", ++i);
                    phaser.arrive();
                } catch (InterruptedException e) {
                    log.info("Interrupt", e);
                }
            }
        }).start();

        for (int i = 0; i < 10; i++) {
            log.info("Await {}...", i);
            phaser.awaitAdvance(i);
            log.info("Done Await {}", i);
        }
    }
}
