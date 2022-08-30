package org.test.mpashka.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class MyObjectSerializer extends StdSerializer<MyData.MyObject> {

    public MyObjectSerializer() {
        super(MyData.MyObject.class);
    }

    @Override
    public void serialize(MyData.MyObject myObject, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
