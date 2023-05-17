package org.mpashka.test.spring.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class MySpringActuatorApp {

    public static void main(String[] args) {
        log.info("Before app start");
        SpringApplication.run(MySpringActuatorApp.class, args);
        log.info("After app start");
    }
}
