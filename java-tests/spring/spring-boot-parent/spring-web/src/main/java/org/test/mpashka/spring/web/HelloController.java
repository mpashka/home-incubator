package org.test.mpashka.spring.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
public class HelloController {
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @Value("${geps.jms.instance.code.selector:'GepsInstanceCode = geps'}")
    String p1;
    @Value("${geps.jms.instance.code.selector:GepsInstanceCode = 'geps'}")
    String p2;
    @Value("${geps.jms.instance.code.selector:GepsInstanceCode = geps}")
    String p3;

    @Autowired
    private RestOperations restOperations;

    @PostConstruct
    private void init() {
        log.info("1:{} 2:{} 3:{}", p1, p2, p3);
    }

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/testRestClient")
    public String testRestClient() {
        String googleResponse = restOperations.getForObject("https://www.google.com", String.class);
        log.info("Google response: {}", googleResponse != null && googleResponse.length() > 10 ? googleResponse.substring(0, 10) : googleResponse);
        return googleResponse;
    }

    @PostMapping("/testPostData")
    public String testPostData(MyPostData data) {
        log.info("Test post data: {}", data);
        return "Ok";
    }


    @Data
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class MyPostData {
        private String aaa;
        private int bbb;
    }
}
