package fr.istic.config;

import java.lang.reflect.Type;
import java.util.Base64;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

public class FooDeserializer implements JsonbDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
       Base64.Decoder decoder = Base64.getDecoder();
       byte[] decodedContent = decoder.decode(parser.getString());
        return decodedContent;
    }

}
