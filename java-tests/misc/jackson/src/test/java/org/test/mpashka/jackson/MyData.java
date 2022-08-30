package org.test.mpashka.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(using = MyDataSerializer.class)
@JsonDeserialize(using = MyDataDeserializer.class)
public class MyData {
    private String field1;
    private int field2;

    private MyObject field3;


    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public int getField2() {
        return field2;
    }

    public void setField2(int field2) {
        this.field2 = field2;
    }

    public MyObject getField3() {
        return field3;
    }

    public void setField3(MyObject field3) {
        this.field3 = field3;
    }

    @JsonSerialize(using = MyObjectSerializer.class)
    @JsonDeserialize(using = MyObjectDeserializer.class)
    public static class MyObject {
        private String f1;
        private int f2;

        public String getF1() {
            return f1;
        }

        public void setF1(String f1) {
            this.f1 = f1;
        }

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }
    }
}
