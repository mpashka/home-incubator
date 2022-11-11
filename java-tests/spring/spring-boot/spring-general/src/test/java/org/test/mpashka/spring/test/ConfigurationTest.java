package org.test.mpashka.spring.test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationTest {
    @Test
    public void testProperties() throws IOException {
        List<PropertySource<?>> properties = new YamlPropertySourceLoader().load("Custom " + getClass() + " source from ", new ClassPathResource("ConfigurationTestConfig.yaml", getClass()));
        CompositePropertySource propertySource = new CompositePropertySource("testConfiguration");
        properties.forEach(propertySource::addPropertySource);
        Binder binder = new Binder(ConfigurationPropertySource.from(propertySource));
        BindResult<TestConfiguration> bind = binder.bind("mine", TestConfiguration.class);

        log.info("Configuration loaded: {}", bind.get());
    }

    @Data
    public static class TestConfiguration {
        private Map<String, Boolean> enable = new LinkedHashMap<>();
    }
}
