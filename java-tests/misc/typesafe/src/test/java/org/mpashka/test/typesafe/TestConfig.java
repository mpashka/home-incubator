package org.mpashka.test.typesafe;

import java.io.File;
import java.net.URL;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigValueType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestConfig {

    @Test
    public void testCast() {
        String resourceBasename = getClass().getPackageName().replace('.', '/') + "/config_example1.conf";
        log.info("Config name: {}", resourceBasename);
        URL resource = getClass().getClassLoader().getResource(resourceBasename);
        log.info("Resource: {}", resource);
        Config config1 = ConfigFactory.load(resourceBasename);
        Config myApp = config1.getConfig("my-app");
        log.info("Test-str: {}", myApp.getString("test-str"));
        myApp.getConfig("fix-config").entrySet().forEach(e -> log.info("{}={} ({})", e.getKey(), e.getValue(), e.getValue().getClass()));
    }

    @Test
    public void testConfig() {
//        Config config = TypesafeConfigLoader.loadConfig("/home/ya-pashka/Projects/arcadia/infra/iss/.tmp/tracing/50_tracing.conf");
        Config config = ConfigFactory.parseFile(
                new File("/home/ya-pashka/Projects/arcadia/infra/iss/.tmp/tracing/50_tracing.conf"),
                ConfigParseOptions.defaults().setAllowMissing(false));
        config.getConfig("agent.tracing.resourceAttrs").entrySet().forEach(e -> {
            if (e.getValue().valueType() != ConfigValueType.STRING) {
                log.error("Invalid value type {}: {}", e.getKey(), e.getValue().valueType());
            } else {
                log.info("Value {}:{}", e.getKey(), e.getValue().unwrapped());
            }
        });
    }
}
