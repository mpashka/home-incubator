package org.mpashka.test.kafka;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;

public class TestAdmin {
    @Test
    public void testAdmin() throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try (Admin admin = Admin.create(props)) {

        }
    }

    private static void createTopic(Admin admin) throws InterruptedException, ExecutionException {
        String topicName = "my-topic";
        int partitions = 12;
        short replicationFactor = 3;
        // Create a compacted topic
        CreateTopicsResult result = admin.createTopics(Collections.singleton(
                new NewTopic(topicName, partitions, replicationFactor)
                        .configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT))));

        // Call values() to get the result for a specific topic
        KafkaFuture<Void> future = result.values().get(topicName);

        // Call get() to block until the topic creation is complete or has failed
        // if creation failed the ExecutionException wraps the underlying cause.
        future.get();
    }

}
