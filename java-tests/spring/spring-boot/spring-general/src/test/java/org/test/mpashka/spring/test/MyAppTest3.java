package org.test.mpashka.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ContextConfiguration(initializers = MyApplicationContextInitializer.class)
@TestPropertySource(properties = {
        "test-enum-value=off"
})
@Slf4j
public class MyAppTest3 {
    @Autowired
    @Qualifier("my-enum-bean")
    private MyConfiguration.MyEnum myEnum;

    @Test
    public void testEnum() {
        log.info("Enum: {}", myEnum);
    }
}
