package org.test.mpashka.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MyRecord(
   int a1,
   String s1
) {
    @JsonCreator
    public MyRecord {
    }

    public String getS2() {
        return "s2";
    }

    public String s3() {
        return "s3";
    }

}
