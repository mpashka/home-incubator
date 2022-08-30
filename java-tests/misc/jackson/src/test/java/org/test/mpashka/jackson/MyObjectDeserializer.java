package org.test.mpashka.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class MyObjectDeserializer extends StdDeserializer<MyData.MyObject> {

    public MyObjectDeserializer() {
        super(MyData.MyObject.class);
    }

    @Override
    public MyData.MyObject deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return null;
    }
}
