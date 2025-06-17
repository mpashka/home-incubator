package org.test.mpashka.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class StreamTest {

    @Test
    public void testThrow() {
        List<String> list = List.of("a", "b");
        try {
            Set<Object> result = list.stream().map(l -> {
                throw new RuntimeException("Hello");
            }).collect(Collectors.toSet());
            log.info("Result: {}", result);
        } catch (Exception e) {
            log.info("Exception", e);
        }
    }

    @Test
    public void testMerge() {
        Map<String, String> map = Stream.of("k1", "k1")
                .collect(Collectors.toMap(v -> "key_" + v, v -> "val_" + v, (v1, v2) -> {
                    log.info("{} {}", v1, v2);
                    return "conflict:" + v1 + "/" + v2;
                }));
        log.info("{}", map);
    }
}
