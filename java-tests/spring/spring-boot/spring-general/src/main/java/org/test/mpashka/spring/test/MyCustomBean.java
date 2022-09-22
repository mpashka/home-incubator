package org.test.mpashka.spring.test;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyCustomBean {

    public MyCustomBean() {
//        log.info("Custom bean constructor", new Throwable("Custom bean constructor"));
    }

    @PostConstruct
    public void init() {
//        log.info("Custom bean init", new Throwable("Custom bean init"));
    }
}
