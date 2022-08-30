package org.test.mpashka.spring.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class HelloController {
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @Value("${geps.jms.instance.code.selector:'GepsInstanceCode = geps'}")
    String p1;
    @Value("${geps.jms.instance.code.selector:GepsInstanceCode = 'geps'}")
    String p2;
    @Value("${geps.jms.instance.code.selector:GepsInstanceCode = geps}")
    String p3;

    @PostConstruct
    private void init() {
        log.info("1:{} 2:{} 3:{}", p1, p2, p3);
    }



    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}
