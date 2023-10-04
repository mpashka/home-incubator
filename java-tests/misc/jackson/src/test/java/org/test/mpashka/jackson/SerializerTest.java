package org.test.mpashka.jackson;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializerTest {

    private static final Logger log = LoggerFactory.getLogger(SerializerTest.class);

    @Test
    public void testSerialize() throws JsonProcessingException {
        MyData myData = new MyData();
        myData.setField1("f1");
        myData.setField2(10);
        MyData.MyObject obj = new MyData.MyObject();
        obj.setF1("inner1");
        obj.setF2(20);
        myData.setField3(obj);

        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(myData);
        log.info(s);

        MyData myData1 = mapper.readValue(s, MyData.class);
        log.info("{}", myData1);
    }

    @Test
    public void testSerializeRecord() throws JsonProcessingException {
        MyRecord record1 = new MyRecord(1, "s1");
        ObjectMapper objectMapper = new ObjectMapper();
        String ser1 = objectMapper.writeValueAsString(record1);
        log.info("Ser1: {}", ser1);

        MyRecord record2 = objectMapper.readValue(ser1, MyRecord.class);
        log.info("Deser1: {}", record2);
    }
}
