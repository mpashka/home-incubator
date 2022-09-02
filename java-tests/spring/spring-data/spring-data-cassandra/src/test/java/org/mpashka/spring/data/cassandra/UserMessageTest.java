package org.mpashka.spring.data.cassandra;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.type.DefaultMapType;

public class UserMessageTest {
    private static final Logger log = LoggerFactory.getLogger(UserMessageTest.class);

    @Test
    public void testRaw() throws UnknownHostException {
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(InetAddress.getByName("kuber-pgu-dev.gosuslugi.local"), 12167))
                .withLocalDatacenter("datacenter1")
                .withKeyspace("geps")
                .withAuthCredentials("cassandra", "cassandra")
                .build())
        {
            {
                ResultSet rs = session.execute("select release_version from system.local");
                Row row = rs.one();
                log.info("Cassandra release: {}", row.getString("release_version"));
            }

            {
                ResultSet rs = session.execute("select * from user_message");
                log.info("user_message definitions");
                for (ColumnDefinition definition : rs.getColumnDefinitions()) {
                    log.info("    Column {} has type {}", definition.getName(), definition.getType());
                }
/*
user_message definitions
    Column oid has type BIGINT
    Column part_idx has type INT
    Column thread_id has type BIGINT
    Column message_id has type BIGINT
    Column data has type UDT(geps.user_message_data)
    Column status_map has type Map(ASCII => UDT(geps.message_status), not frozen)
 */
                log.info("---");
                boolean firstRow = true;
                for (Row row : rs) {
                    if (firstRow) {
                        firstRow = false;
                    }
                    log.info("Row: {}", row);
                }
            }


            PreparedStatement ps = session.prepare("UPDATE user_message" +
                    " SET status_map = status_map + :statuses" +
                    " WHERE oid = :userOid AND part_idx = :partIdx AND thread_id = :threadId AND message_id = :messageId");
            log.info("Update user_message statuses definitions");
            for (ColumnDefinition definition : ps.getVariableDefinitions()) {
                log.info("    Column {} has type {}", definition.getName(), definition.getType());
            }
            ColumnDefinition statusesDefinition = ps.getVariableDefinitions().get("statuses");
            log.info("Statuses: {}", statusesDefinition.getType().asCql(true, true));
            log.info("Statuses class: {}", statusesDefinition.getType().getClass());
            DefaultMapType statusesType = (DefaultMapType) statusesDefinition.getType();
            log.info("Statuses Map: {} -> {}", statusesType.getKeyType(), statusesType.getValueType());
            log.info("---");

            UserDefinedType udt = session.getMetadata()
                    .getKeyspace("geps")
                    .flatMap(ks -> ks.getUserDefinedType("message_status"))
                    .orElseThrow(() -> new IllegalArgumentException("Missing UDT definition"));
            log.info("Message status UDT: {}", udt);
            log.info("   Field names[{}]: {}", udt.getFieldNames().size(), udt.getFieldNames());
            log.info("   Field types[{}]: {}", udt.getFieldTypes().size(), udt.getFieldTypes());

                /*
Message status UDT: UDT(geps.message_status)
   Field names[5]: [status, oid, status_date, env_id, user_session_id]
   Field types[5]: [ASCII, BIGINT, TIMESTAMP, TEXT, TEXT]
Update user_message statuses definitions
    Column statuses has type Map(ASCII => UDT(geps.message_status), not frozen)
    Column useroid has type BIGINT
    Column partidx has type INT
    Column threadid has type BIGINT
    Column messageid has type BIGINT
Statuses: map<ascii, geps.message_status>
Statuses class: class com.datastax.oss.driver.internal.core.type.DefaultMapType
Statuses Map: ASCII -> UDT(geps.message_status)
                 */

            {
                UdtValue udtValue = udt.newValue("Hello", 1L, Instant.now(), "env1", "session1");

                BoundStatement boundStatement = ps.boundStatementBuilder()
                        .setLong("userOid", 100)
                        .setInt("partIdx", 1)
                        .setLong("threadId", 101)
                        .setLong("messageId", 102)
                        .set("statuses", Map.of("Hello", udtValue), GenericType.mapOf(GenericType.STRING, GenericType.UDT_VALUE))
                        .build();

                session.execute(boundStatement);
            }

            {
                BoundStatement boundStatement = ps.boundStatementBuilder()
                        .setLong("userOid", 100)
                        .setInt("partIdx", 1)
                        .setLong("threadId", 101)
                        .setLong("messageId", 102)
                        .set("statuses", Map.of("Hello", UserMessage.MessageStatus.builder()
                                .oid(2)
                                .envId("env2")
                                .userSessionId("session2")
                                .build()), GenericType.mapOf(String.class, UserMessage.MessageStatus.class))
                        .build();
                session.execute(boundStatement);
            }
        }
    }
}
