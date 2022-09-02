package org.mpashka.spring.data.cassandra;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Frozen;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(UserMessage.TABLE_NAME)
public class UserMessage {
    public static final String TABLE_NAME = "user_message";

    public static final String COL_STATUS_MAP = "status_map";
    public static final String COL_DATA = "data";

    /**
     * Количество сообщений в одной партиции
     */
    private static final int MESSAGES_PER_PARTITION = 500_000;


    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @PrimaryKeyClass
    public static class Key {
        /**
         * OID пользователя.
         */
        @PrimaryKeyColumn(value = "oid", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
        private long oid;

        /**
         * Индекс партиции данных почтового ящика пользователя.
         */
        @PrimaryKeyColumn(value = "part_idx", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
        private int partIdx;

        /**
         * ID цепочки.
         */
        @PrimaryKeyColumn(value = "thread_id", type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING)
        private long threadId;

        /**
         * ID сообщения.
         */
        @PrimaryKeyColumn(value = "message_id", type = PrimaryKeyType.CLUSTERED, ordinal = 3, ordering = Ordering.DESCENDING)
        private long messageId;
    }

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
//    @Frozen
    @UserDefinedType(MessageStatus.TYPE_NAME)
    public static class MessageStatus {

        public static final String TYPE_NAME = "message_status";

        public static final String DT_OID = "oid";
        public static final String DT_STATUS_DATE = "status_date";
        public static final String DT_ENV_ID = "env_id";
        public static final String DT_USER_SESSION_ID = "user_session_id";

        @Column("status")
        private String status;

        /**
         * OID пользователя, сменившего статус сообщения.
         * Для ЮЛ - oid ого ФЛ, который авторизовался как сотрудник и изменил статус сообщения.
         */
        @Column(DT_OID)
        private long oid;

        @Column(DT_STATUS_DATE)
        private Date statusDate;

        @Column(DT_ENV_ID)
        private String envId;

        @Column(DT_USER_SESSION_ID)
        private String userSessionId;

    }

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @UserDefinedType(Data.TYPE_NAME)
    public static class Data {
        public static final String TYPE_NAME = "user_message_data";

        public static final String DT_WS_TEMPLATE_ID = "ws_template_id";

        @CassandraType(type = Name.INT)
        @Column(DT_WS_TEMPLATE_ID)
        private Integer wsTemplateId;

        @CassandraType(type = Name.VARCHAR)
        @Column(value = "ws_template_type")
        private String wsTemplateType;

        @CassandraType(type = Name.BIGINT)
        @Column(value = "from_oid")
        private Long fromOid;

        @CassandraType(type = Name.VARCHAR)
        @Column(value = "from_user_name")
        private String fromUserName;

        @CassandraType(type = Name.VARCHAR)
        @Column(value = "uin")
        private String uin;

        @CassandraType(type = Name.VARCHAR)
        @Column(value = "subject")
        private String subject;

        @CassandraType(type = Name.VARCHAR)
        @Column(value = "join_param")
        private String joinParam;

        @CassandraType(type = Name.ASCII)
        @Column(value = "cat")
        private String cat;

        @CassandraType(type = Name.ASCII)
        @Column(value = "hash")
        private String hash;

        @CassandraType(type = Name.TIMESTAMP)
        @Column(value = "create_date")
        private Date createDate;

        @CassandraType(type = Name.TIMESTAMP)
        @Column(value = "update_date")
        private Date updateDate;

    }

    @PrimaryKey
    private Key id;

    @CassandraType(type = Name.MAP, typeArguments = {Name.ASCII, Name.UDT}, userTypeName = MessageStatus.TYPE_NAME)
    @Column(COL_STATUS_MAP)
    private Map<String, @Frozen MessageStatus> statusMap;

    @Column(COL_DATA)
    private Data data;
}
