package org.mpashka.test.spring.actuator;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Order(5)
public class MyConfiguration2 {

    {
        log.info("Create myConfiguration2!");
    }

    @PostConstruct
    public void init() {
        log.info("Init myConfiguration2!");
    }

    @Bean
    @Order(1)
    public MyBean2 myBean2() {
        log.info("Configure myBean2!");
        return new MyBean2();
    }

    public static class MyBean2 {
        {
            log.info("Create myBean2!");
        }

        @PostConstruct
        public void init() {
            log.info("Init myBean2!");
        }
    }
}
