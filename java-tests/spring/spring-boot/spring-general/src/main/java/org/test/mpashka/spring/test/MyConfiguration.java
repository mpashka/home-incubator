package org.test.mpashka.spring.test;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(value = {
        MyConfiguration.MyConfigurationProperties.class
})
@Slf4j
public class MyConfiguration {
    public MyConfiguration() {
//        log.info("Configuration", new Throwable("Configuration"));
    }

    @PostConstruct
    public void init() {
//        log.info("Configuration.init", new Throwable("Configuration.init"));
    }

    @Bean
    public MyConfigurationBean myConfigurationBean() {
        return new MyConfigurationBean();
    }

    @Bean("my-enum-bean")
    public MyEnum myTestPropsBean(@Value("${test-enum-value:off}") MyEnum enumValue) {
        log.info("EnumValue: {}", enumValue);
        return enumValue;
    }

    @Slf4j
    public static class MyConfigurationBean {
        public MyConfigurationBean() {
//            log.info("MyConfigurationBean bean", new Throwable("MyConfigurationBean bean"));
        }

        @PostConstruct
        public void init() {
//            log.info("MyConfigurationBean bean.init", new Throwable("MyConfigurationBean bean.init"));
        }
    }

    public enum MyEnum {
        on, off, unknown
    }

    @ConfigurationProperties(prefix = "my.test-props")
    @Data
    public static class MyConfigurationProperties {
        private MyConfigurationObject[] arrayOfObjects;
        private MyConfigurationObject singleObject;
        private String strProp;
        private int intProp;
        private String strProp2;
        private String strProp3;
    }

    @Data
    public static class MyConfigurationObject {
        private String strProp;
        private int intProp;
        private String strProp2;
        private String strProp3;
    }
}
