package org.mpashka.test.kafka;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import lombok.extern.slf4j.Slf4j;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class KafkaTest {

    private static final String bootstrap = "localhost:9092";
    private static final String topic = "foo";
    private static final String groupId = "my-kafka-test";
//    private static final String groupId = "test-Smev3RequestMessageRegistryV201HandlerTest";
//    private static final String topic = "geps-smev3-in-response";

    private Producer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private Admin admin;

    //
    //
    //
    private List<PartitionInfo> partitionInfos;
    private List<TopicPartition> topicPartitions;
    private Map<TopicPartition, Long> endOffsets;


    @BeforeAll
    public void init() throws Exception {
        {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producer = new KafkaProducer<>(props);
        }

        {
            Properties props = new Properties();
            props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
            props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none"); // latest, none, earliest
            props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
            consumer = new KafkaConsumer<>(props);
        }

        {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
            admin = Admin.create(props);
        }
    }

    @Test
    public void testProduceConsumeAdmin() throws Exception {
        listConsumerGroups("Consumer groups before subscribe");

//        consumer.subscribe(Arrays.asList(topic));
//        listConsumerGroups("Consumer groups after subscribe");
//        Thread.sleep(3000);
//        listConsumerGroups("Consumer groups after 3 seconds");

        listPartitions();
        offsetAtTime();
        endOffsets("Begin");

        consumer.assign(topicPartitions);

        currentPositions();
//        topicPartitions.forEach(p -> consumer.assign(List.of(p)));
        topicPartitions.forEach(p -> consumer.seek(p, endOffsets.get(p) - 1));

        String value = "World " + new Date();
        log.info("Sending {} ...", value);
        producer.send(new ProducerRecord<>(topic, "Hello", value)).get(10, TimeUnit.SECONDS);

        log.info("Receiving ...");
        ConsumerRecords<String, String> records = consumer.poll(Duration.of(3, ChronoUnit.SECONDS));
        log.info("Received {} records", records.count());
        for (ConsumerRecord<String, String> record : records) {
            log.info("    offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
        }
        endOffsets("Before commit");
        consumer.commitSync();
        listConsumerGroups("Consumer groups after poll");
        endOffsets("After commit");
    }

    @Test
    public void testSubscribeAndSeek() throws Exception {
        listPartitions();
//        admin.deleteConsumerGroupOffsets(groupId, new HashSet<>(topicPartitions));
        endOffsets("Before assign");

        Map<TopicPartition, OffsetAndMetadata> consumerOffsets = admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
        log.info("Consumer admin offsets: {}", consumerOffsets.size());
        consumerOffsets.forEach((k, v) -> log.info("    {}: {}", k, v));

        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
//        consumer.seekToEnd(topicPartitions);
        consumer.assign(topicPartitions);

        topicPartitions.forEach(p -> {
            OffsetAndMetadata consumerOffset = consumerOffsets.get(p);
            Long endOffset = endOffsets.get(p);
            if (endOffset == null) {
                if (consumerOffset != null) {
                    log.warn("End offset is null while consumer offset is not for {}", p);
                }
                consumer.seek(p, 0);
            } else if (consumerOffset == null) {
                consumer.seek(p, endOffset);
            } else if (consumerOffset.offset() != endOffset) {
                log.info("Skip {} records in partition: {}-{}", endOffset-consumerOffset.offset(), p.topic(), p.partition());
                consumer.seek(p, endOffset);
            }
        });
//        admin.alterConsumerGroupOffsets()


        currentPositions();
        consumer.commitSync();
    }

    private void currentPositions() {
        log.info("Current positions");
        topicPartitions.forEach(p -> log.info("    {}: {}", p, consumer.position(p)));

//        admin.listOffsets()
//        admin.listTopics()
//        admin.listConsumerGroups()
//        admin.deleteRecords()
//        admin.
    }

    private void listConsumerGroups(String name) throws InterruptedException, ExecutionException {
        log.info(name);
        admin.listConsumerGroups().all().get().stream()
                .filter(g -> g.groupId().equals(groupId)).forEach(
                        g -> log.info("    Group: {}", g)
                );
    }

    private void listPartitions() {
        log.info("Partitions:");
        partitionInfos = consumer.partitionsFor(topic);
        partitionInfos.forEach(p -> log.info("    {}", p));
        topicPartitions = partitionInfos.stream().map(p -> new TopicPartition(topic, p.partition())).toList();
    }

    private void offsetAtTime() {
        log.info("Generic Offsets -1 hour");
        long time = Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli();
        Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(
                topicPartitions.stream().collect(Collectors.toMap(p -> p, p -> time)));
        offsets.forEach((p, o) -> log.info("    {}: {}", p, o));
    }

    private void endOffsets(String step) {
        log.info("End Offsets {}", step);
        endOffsets = consumer.endOffsets(topicPartitions);
        endOffsets.forEach((p, o) -> log.info("    {}: {}", p, o));
    }
}
