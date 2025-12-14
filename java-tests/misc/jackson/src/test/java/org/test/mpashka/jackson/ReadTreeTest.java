package org.test.mpashka.jackson;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ReadTreeTest {
    @Test
    public void testReadArray() throws IOException {
        JsonNode json = new ObjectMapper().readTree(new File("/home/ya-pashka/Projects/arcadia.docs/tasks/metrics/server_info.json"));
        Iterator<JsonNode> walleTags = json.get("walle_tags").elements();
        while (walleTags.hasNext()) {
            JsonNode tag = walleTags.next();
            log.info("Tag: {} {}, {}", tag.getClass(), tag, tag.asText());
        }

    }
}
