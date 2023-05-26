package org.mpashka.spring.data.cassandra;

import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SessionBuilderConfigurer;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.type.UserDefinedTypeBuilder;
import com.datastax.oss.driver.shaded.guava.common.collect.Lists;

@Configuration
@EnableCassandraRepositories(basePackages = "org.mpashka.spring.data.cassandra")
public class CassandraConfig extends AbstractCassandraConfiguration {
    @Override
    protected String getKeyspaceName() {
        return "geps";
    }

/*
    @Override
    public CassandraCustomConversions customConversions() {
        return new CassandraCustomConversions(
                Lists.newArrayList(
                        new ApplicationIdWriteConverter(), new ApplicaitonIdReadConverter(),
                        new UserIdWriteConverter(), new UserIdReadConverter())
        );
    }
*/

    @Override
    protected SessionBuilderConfigurer getSessionBuilderConfigurer() {
        return clusterBuilder -> {
            clusterBuilder.addTypeCodecs(new AttachmentDataItemCodec());
            return clusterBuilder;
        };
    }

    @Bean
    CqlSessionBuilderCustomizer addCustomCodecs(final CassandraProperties properties) {

        return (cqlSessionBuilderCustomizer) -> {

            cqlSessionBuilderCustomizer.addTypeCodecs(
                    new AttachmentDataItemCodec()
            );
        };
    }
}
