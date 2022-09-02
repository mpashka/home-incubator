package org.mpashka.spring.data.cassandra;

import java.util.Map;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

/**
 * Управление данными пользовательских сообщений (почтовый ящик).
 */
public interface UserMessageRepository extends CassandraRepository<UserMessage, UserMessage.Key> {

    @Query("UPDATE user_message"
            + " SET status_map[?4] = ?5"
            + " WHERE oid = ?0 AND part_idx = ?1 AND thread_id = ?2 AND message_id = ?3"
            + " IF status_map[?4] = NULL")
    boolean addUserMessageStatus(long oid, int partIdx, long threadId, long messageId,
            String mnemonic, UserMessage.MessageStatus status);

    @Query("UPDATE user_message"
            + " SET status_map = status_map + ?4"
            + " WHERE oid = ?0 AND part_idx = ?1 AND thread_id = ?2 AND message_id = ?3")
    boolean addUserMessageStatuses(long oid, int partIdx, long threadId, long messageId, Map<String, UserMessage.MessageStatus> statuses);
}
