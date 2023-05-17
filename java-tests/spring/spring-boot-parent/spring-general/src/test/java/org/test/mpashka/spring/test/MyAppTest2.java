package org.test.mpashka.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ContextConfiguration(initializers = MyApplicationContextInitializer.class)
@Slf4j
public class MyAppTest2 {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private MyApplicationContextInitializer.MyFactoryBeanData myProducedBean;

//    @Autowired
//    private MyApplicationContextInitializer.MyTestBean myTestBean;

    @Test
    public void testAppOrder() {
        log.info("Application context2: {}", context);

        log.info("MyProducedBean2: {}", myProducedBean);
    }
}
