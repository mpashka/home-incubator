package org.test.mpashka.spring.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
//@EnableConfigurationProperties(MyProperties.class)
@ConfigurationPropertiesScan
//@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

//    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            log.info("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                log.info("    {}", beanName);
            }
        };
    }

    @Bean("MyProp1")
    public String myBean(MyProperties myProperties) {
        log.info("My properties: {}", myProperties);
        return "aaa";
    }

    @Bean("MyProp2")
    public String myBean(CacheProperties cacheProperties) {
        log.info("Cache properties: {}", cacheProperties);
        return "bbb";
    }
}
