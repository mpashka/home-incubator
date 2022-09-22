package org.test.mpashka.spring.test;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MyBean {
    public MyBean() {
//        log.info("My bean", new Throwable("My bean"));
    }

    @PostConstruct
    public void init() {
//        log.info("My bean.init", new Throwable("My bean.init"));
    }
}
