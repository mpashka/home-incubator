package org.mpashka.totemftc.api;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;
import java.util.Map;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger log = LoggerFactory.getLogger(PostgresResource.class);

    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("totemftc")
                    .withUsername("totemftc")
                    .withPassword("totemftc")
                    .withClasspathResourceMapping("sql/db.sql", "/docker-entrypoint-initdb.d/01_db.sql", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("sql/db_functions.sql", "/docker-entrypoint-initdb.d/02_db_functions.sql", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("sql/db_data.sql", "/docker-entrypoint-initdb.d/03_db_data.sql", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("sql/db_test_data.sql", "/docker-entrypoint-initdb.d/04_db_test_data.sql", BindMode.READ_ONLY)
            ;

    @Override
    public Map<String, String> start() {
        if (true) return null;


        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        System.out.println("Logger factory " + iLoggerFactory);

        log.debug("Debug");
        log.info("Info");
        log.warn("Warn");
        log.error("Error");

/*
        var logger = java.util.logging.Logger.getLogger(PostgresResource.class.getName());
        logger.info("jul Info");
        logger.severe("jul Severe");
        logger.config("jul Config");
*/

        db.start();
        return Collections.singletonMap("quarkus.datasource.url", db.getJdbcUrl());
    }

    @Override
    public void stop() {
        db.stop();
    }
}
