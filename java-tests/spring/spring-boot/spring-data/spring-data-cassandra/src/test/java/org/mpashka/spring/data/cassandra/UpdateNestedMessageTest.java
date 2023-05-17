package org.mpashka.spring.data.cassandra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.SessionCallback;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.core.query.Update;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@Slf4j
public class UpdateNestedMessageTest {
    @Autowired
    private CassandraOperations cql;

    @Test
    public void testUpdateNestedField() {
        UserMessage.Key key = UserMessage.Key.builder()
                .oid(5)
                .partIdx(0)
                .threadId(15)
                .messageId(15)
                .build();

        cql.insert(UserMessage.builder()
                .id(key)
                .data(UserMessage.Data.builder()
                        .subject("Hello1")
                        .build())
                .build());

        UserMessage msg1 = cql.selectOneById(key, UserMessage.class);
        assertThat(msg1, notNullValue());
        assertThat(msg1.getData().getSubject(), is("Hello1"));

        cql.getCqlOperations().execute("UPDATE user_message SET data.subject='Hello2' WHERE oid=5 and part_idx=0 and thread_id=15 and message_id=15");
        UserMessage msg2 = cql.selectOneById(key, UserMessage.class);
        assertThat(msg2.getData().getSubject(), is("Hello2"));

        cql.getCqlOperations().execute((SessionCallback<Object>) s -> {
            PreparedStatement prepared = s.prepare("UPDATE user_message SET data.subject=:subject WHERE oid=:oid and part_idx=:partIdx and thread_id=:threadId and message_id=:messageId");
            BoundStatement bound = prepared.bind()
                    .setString("subject", "Hello3")
                    .setLong("oid", 5)
                    .setLong("partIdx", 0)
                    .setLong("threadId", 15)
                    .setLong("messageId", 15);
            ResultSet result = s.execute(bound);
            result.forEach(r -> log.info("Result: {}", r));
            return result;
        });

        UserMessage msg3 = cql.selectOneById(key, UserMessage.class);
        assertThat(msg3.getData().getSubject(), is("Hello3"));

        cql.update(Query.query(
                Criteria.where("oid").is(5),
                Criteria.where("part_idx").is(0),
                Criteria.where("thread_id").is(15),
                Criteria.where("message_id").is(15)
        ), Update.update(UserMessage.COL_DATA, UserMessage.Data.builder()
                .subject("Hello4")
                .build()
        ), UserMessage.class);

        UserMessage msg4 = cql.selectOneById(key, UserMessage.class);
        assertThat(msg4.getData().getSubject(), is("Hello4"));

        cql.update(Query.query(
                Criteria.where("oid").is(5),
                Criteria.where("part_idx").is(0),
                Criteria.where("thread_id").is(15),
                Criteria.where("message_id").is(15)
        ), Update.update(UserMessage.COL_DATA + ".subject", UserMessage.Data.builder()
                .subject("Hello5")
                .build()
        ), UserMessage.class);

        UserMessage msg5 = cql.selectOneById(key, UserMessage.class);
        assertThat(msg5.getData().getSubject(), is("Hello5"));
    }
}
