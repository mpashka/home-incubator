package org.test.mpashka.jackson.kotlin

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class TestSerialize {
    val log = LoggerFactory.getLogger(TestSerialize::class.java)

    @Test
    fun testSerialize() {
        val a = SimpleData("aaa", 1, 2)
        log.info("a: {}", a)

        val objectMapper = ObjectMapper()
        val res = objectMapper.writeValueAsString(a)
        log.info("'{}'", res)

        val a1 = objectMapper.readValue(res, SimpleData::class.java);

        log.info("{} -> '{}' -> {}", a, res, a1)
    }
}