package org.mpashka.test.spring.actuator;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Order(10)
@DependsOn("myConfiguration2")
public class MyConfiguration {

    {
        log.info("Create myConfiguration!");
    }

    @PostConstruct
    public void init() {
        log.info("Init myConfiguration!");
    }

/*
    @Bean("myBean2")
    public MyConfiguration2.MyBean myBean() {
        return new MyConfiguration2.MyBean();
    }
*/

    @Bean("myStringBean")
    @ConditionalOnBean(MyConfiguration2.MyBean2.class)
    public MyMeterBean myMeterBean(MeterRegistry registry) {
        log.info("Configure myMeterBean!");
        log.info("Registry: {}", registry);
        Timer timer = registry.timer("my-timer", List.of(
                Tag.of("tag1", "val1")
        ));
        timer.record(10, TimeUnit.SECONDS);
        log.info("Timer: {}", timer);

        return new MyMeterBean();
    }

    @Bean("justCheck")
    @Order(100)
    public String myCheckBean(ApplicationContext ctx/*, MyConfiguration2.MyBean2 myBean2*/) {
        ConfigurableApplicationContext cctx =(ConfigurableApplicationContext) ctx;
        ConfigurableListableBeanFactory beanFactory = cctx.getBeanFactory();
        log.info("Contains myBean2: {}", beanFactory.containsBeanDefinition("myBean2"));
        return "aaa";
    }

    public static class MyMeterBean {
        {
            log.info("Create myMeterBean!");
        }

        @PostConstruct
        public void init() {
            log.info("Init myMeterBean!");
        }
    }
}
