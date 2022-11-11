package org.mpashka.test.geps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckStats {
    // 18.10.22 12:26:19,214000000
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss,SSS000000");

    public static void main(String[] args) throws Exception {
        File dir = new File("/home/pmukhataev/Projects/rtc/my-tasks/EPGUCORE-113753_NoAnswer/2");
        Map<String, Message> messages = new HashMap<>();

        try (Reader in = new FileReader(new File(dir, "MESSAGE_ID.csv"))) {
            for (CSVRecord record : CSVFormat.EXCEL.withHeader().parse(in)) {
                String messageId = record.get("MESSAGE_ID");
                TemporalAccessor receive = format.parse(record.get("RECEIVE_TS"));
                TemporalAccessor status = format.parse(record.get("STATUS_TS"));
                messages.put(messageId, new Message(messageId, receive, status, new HashMap<>()));
            }
        }

        try (Reader in = new FileReader(new File(dir, "JMS_CORRELATION_ID.csv"))) {
            for (CSVRecord record : CSVFormat.EXCEL.withHeader().parse(in)) {
                String messageId = record.get("MESSAGE_ID");
                TemporalAccessor receiveTime = format.parse(record.get("RECEIVE_TS"));
                TemporalAccessor statusTime = format.parse(record.get("STATUS_TS"));
                String status = record.get("STATUS");
                String jmsCorrelationId = record.get("JMS_CORRELATION_ID");
                String dc = record.get("DC");

                SaveKey key = new SaveKey(status, dc);
                messages.computeIfAbsent(jmsCorrelationId, j -> new Message(j, null, null, new HashMap<>()))
                        .getCount().computeIfAbsent(key, k -> new AtomicInteger())
                        .incrementAndGet();
            }
        }

        for (Message message : messages.values()) {
            if (message.getCount().size() > 1) {
                log.info("{} -> {}", message.getMessageId(), message.getCount().size());
            }
/*
            boolean processed = message.getCount().keySet().stream().anyMatch(k -> k.getStatus().equals("B"));
            if (!processed) {
                log.info("Unprocessed message: {}", message.getMessageId());
                message.getCount().forEach((k, v) -> {
                    log.info("    {} -> {}", k, v);
                });
            }
*/
        }

        long unprocessed = messages.values().stream()
                .filter(m -> m.getCount().keySet().stream().noneMatch(k -> k.getStatus().equals("B")))
                .count();
        log.info("Unprocessed: {}", unprocessed);

        Map<AllKey, AtomicInteger> allKeys = new HashMap<>();
        for (Message message : messages.values()) {
            message.getCount().forEach((k, v) -> {
                AllKey allKey = new AllKey(k.getStatus(), k.getDc(), v.get());
                allKeys.computeIfAbsent(allKey, a -> new AtomicInteger()).incrementAndGet();
            });
        }

        allKeys.forEach((k, v) -> {
            log.info("{} -> {}", k, v);
        });
    }

    @Data
    @AllArgsConstructor
    public static class Message {
        private String messageId;
        private TemporalAccessor receive;
        private TemporalAccessor status;

        private Map<SaveKey, AtomicInteger> count;
    }

    @Data
    @AllArgsConstructor
    public static class SaveKey {
        private String status;
        private String dc;
    }

    @Data
    @AllArgsConstructor
    public static class AllKey {
        private String status;
        private String dc;
        private int count;
    }
}
