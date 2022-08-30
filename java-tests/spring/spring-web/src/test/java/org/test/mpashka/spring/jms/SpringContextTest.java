package org.test.mpashka.spring.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = "classpath:spring-test-root.xml")
//@ActiveProfiles(ProfilesDictionary.CACHE_DISABLE)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpringContextTest {
    private static final Logger log = LoggerFactory.getLogger(SpringContextTest.class);

    @Test
    public void test() {
        log.info("Hello world");
    }
}
