package org.test.mpashka.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonSerialize(using = MyDataSerializer.class)
//@JsonDeserialize(using = MyDataDeserializer.class)
@Data
public class MyData {
    private String field1;
    private int field2;

    private MyObject field3;

//    @JsonSerialize(using = MyObjectSerializer.class)
//    @JsonDeserialize(using = MyObjectDeserializer.class)
    @Data
    public static class MyObject {
        private String f1;
        private int f2;
    }
}
