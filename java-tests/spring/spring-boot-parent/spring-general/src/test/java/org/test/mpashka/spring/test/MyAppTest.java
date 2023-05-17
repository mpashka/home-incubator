package org.test.mpashka.spring.test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.test.context.support.TestPropertySourceUtils.INLINED_PROPERTIES_PROPERTY_SOURCE_NAME;

@SpringBootTest
@ContextConfiguration(initializers = MyApplicationContextInitializer.class)
@Slf4j
public class MyAppTest {
    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    @Qualifier("&myBean")
    private MyApplicationContextInitializer.MyFactoryBean myFactoryBean;

    @Autowired
    @Qualifier("myBean")
    private MyApplicationContextInitializer.MyFactoryBeanData myProducedBean;

//    @Autowired
//    private MyApplicationContextInitializer.MyTestBean myTestBean;

    @Autowired
    private MyConfiguration.MyConfigurationProperties configurationProperties;

    @Test
    public void testAppOrder() {
        log.info("Application context1: {}", context);

        log.info("MyProducedBean1: {}", myProducedBean);
    }

    @Test
    public void testConfig() {
        log.info("Configuration: {}", configurationProperties);

        TreeMap<String, Object> properties = context.getEnvironment().getPropertySources().stream().filter(p -> p instanceof EnumerablePropertySource)
                .map(p -> (EnumerablePropertySource<?>) p)
                .flatMap(p ->
                        Arrays.stream(p.getPropertyNames())
                                .filter(n -> n.startsWith("my.test-props.array-of-objects"))
                                .map(n -> Map.entry(n, p.getProperty(n) + " (" + p.getName() + ")"))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (m1, m2) -> m1, TreeMap::new));
        properties.forEach((n, v) -> log.info("    {}: {}", n, v));

    }
}
