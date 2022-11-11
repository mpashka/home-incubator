package org.test.mpashka.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class MyDataDeserializer extends StdDeserializer<MyData> {

    public MyDataDeserializer() {
        super(MyData.class);
    }

    @Override
    public MyData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
//        deserializationContext.
//        return deserializationContext.readValue();
        return null;
    }
}
