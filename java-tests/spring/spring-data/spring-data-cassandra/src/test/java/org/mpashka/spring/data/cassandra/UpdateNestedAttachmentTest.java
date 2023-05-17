package org.mpashka.spring.data.cassandra;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.SessionCallback;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.core.query.Update;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.type.codec.BigIntCodec;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@Slf4j
public class UpdateNestedAttachmentTest {
    @Autowired
    private CassandraOperations cql;

    private Attachment.Key key;

    @BeforeEach
    public void init() {
        key = Attachment.Key.builder()
                .messageId(5)
                .attachmentId(6)
                .build();

        cql.insert(Attachment.builder()
                .id(key)
                .data(Attachment.AttachmentData.builder()
                        .status("Hello1")
                        .build())
                .build());
    }

    @Test
    public void testUpdateNestedObject() {
        ((MutableCodecRegistry) cql.getConverter().getCodecRegistry()).register(new BigintIntegerCodec());
        /*
        Produces com.datastax.oss.driver.api.core.type.codec.CodecNotFoundException: Codec not found for requested operation: [BIGINT <-> java.lang.Integer]
        without codec
         */
        cql.update(Query.query(
                Criteria.where(Attachment.Key.MESSAGE_ID).is(5),
                Criteria.where(Attachment.Key.ATTACHMENT_ID).is(6)
        ), Update.update(Attachment.DATA, Attachment.AttachmentData.builder()
                .status("Hello4")
                .build()
        ), Attachment.class);

        Attachment msg4 = cql.selectOneById(key, Attachment.class);
        assertThat(msg4.getData().getStatus(), is("Hello4"));
    }

    @Test
    public void testUpdateNestedField() {

        Attachment msg1 = cql.selectOneById(key, Attachment.class);
        assertThat(msg1, notNullValue());
        assertThat(msg1.getData().getStatus(), is("Hello1"));

        cql.getCqlOperations().execute("UPDATE attachment SET data.status='Hello2' WHERE message_id=5 and attachment_id=6");
        Attachment msg2 = cql.selectOneById(key, Attachment.class);
        assertThat(msg2.getData().getStatus(), is("Hello2"));

        cql.getCqlOperations().execute((SessionCallback<Object>) s -> {
            PreparedStatement prepared = s.prepare("UPDATE attachment SET data.status=:status WHERE message_id=:messageId and attachment_id=:attachmentId");
            BoundStatement bound = prepared.bind()
                    .setString("status", "Hello3")
                    .setLong("messageId", 5)
                    .setLong("attachmentId", 6);
            ResultSet result = s.execute(bound);
            result.forEach(r -> log.info("Result: {}", r));
            return result;
        });

        Attachment msg3 = cql.selectOneById(key, Attachment.class);
        assertThat(msg3.getData().getStatus(), is("Hello3"));

        /*
        org.springframework.data.cassandra.CassandraInvalidQueryException: Query; CQL [UPDATE attachment SET status=? WHERE message_id=? AND attachment_id=?];
        Undefined column name status in table geps.attachment;
        nested exception is com.datastax.oss.driver.api.core.servererrors.InvalidQueryException: Undefined column name status in table geps.attachment
         */
        cql.update(Query.query(
                Criteria.where(Attachment.Key.MESSAGE_ID).is(5),
                Criteria.where(Attachment.Key.ATTACHMENT_ID).is(6)
        ), Update.update(Attachment.DATA + '.' + Attachment.AttachmentData.DT_STATUS, Attachment.AttachmentData.builder()
                .status("Hello5")
                .build()
        ), Attachment.class);

        Attachment msg5 = cql.selectOneById(key, Attachment.class);
        assertThat(msg5.getData().getStatus(), is("Hello5"));
    }

    /**
     * Reports:
     * org.springframework.data.cassandra.CassandraInvalidQueryException: Query; CQL [UPDATE attachment SET "data.status"=? WHERE message_id=? AND attachment_id=?]; Undefined column name "data.status" in table geps.attachment; nested exception is com.datastax.oss.driver.api.core.servererrors.InvalidQueryException: Undefined column name "data.status" in table geps.attachment
     */
    @Test
    public void testUpdateNestedFieldQuotes() {
        cql.update(Query.query(
                Criteria.where(Attachment.Key.MESSAGE_ID).is(5),
                Criteria.where(Attachment.Key.ATTACHMENT_ID).is(6)
        ), Update.update('"' + Attachment.DATA + '.' + Attachment.AttachmentData.DT_STATUS + '"', Attachment.AttachmentData.builder()
                .status("Hello5")
                .build()
        ), Attachment.class);

        Attachment msg5 = cql.selectOneById(key, Attachment.class);
        assertThat(msg5.getData().getStatus(), is("Hello5"));
    }

    /**
     * @see BigIntCodec
     */
    public static class BigintIntegerCodec implements TypeCodec<Integer> {
        @NonNull
        @Override
        public GenericType<Integer> getJavaType() {
            return GenericType.INTEGER;
        }

        @NonNull
        @Override
        public DataType getCqlType() {
            return DataTypes.BIGINT;
        }

        @Nullable
        @Override
        public ByteBuffer encode(Integer value, @NonNull ProtocolVersion protocolVersion) {
            if (value == null) {
                return null;
            }
            ByteBuffer bytes = ByteBuffer.allocate(8);
            bytes.putLong(0, value);
            return bytes;
        }

        @Override
        public Integer decode(@Nullable ByteBuffer bytes, @NonNull ProtocolVersion protocolVersion) {
            if (bytes == null || bytes.remaining() == 0) {
                return null;
            }
            if (bytes.remaining() != 8) {
                throw new IllegalArgumentException(
                        "Invalid 32-bits long value, expecting 8 bytes but got " + bytes.remaining());
            } else {
                return (int) bytes.getLong(bytes.position());
            }
        }

        @NonNull
        @Override
        public String format(@Nullable Integer value) {
            return (value == null) ? "NULL" : Integer.toString(value);
        }

        @Nullable
        @Override
        public Integer parse(@Nullable String value) {
            try {
                return (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL"))
                        ? null
                        : Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("Cannot parse 32-bits long value from \"%s\"", value));
            }
        }
    }
}
