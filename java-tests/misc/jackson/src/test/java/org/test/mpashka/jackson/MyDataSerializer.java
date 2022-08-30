package org.test.mpashka.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class MyDataSerializer extends StdSerializer<MyData> {

    public MyDataSerializer() {
        super(MyData.class);
    }

    @Override
    public void serialize(MyData myData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        serializerProvider.defaultSerializeValue(myData, jsonGenerator);
    }
}
