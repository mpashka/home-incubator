package org.mpashka.spring.data.cassandra;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "org.mpashka.spring.data.cassandra")
public class CassandraConfig {
}
