package fr.istic.config;

import java.util.Base64;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;


public class FooSerializer implements JsonbSerializer<byte[]> {

    @Override
    public void serialize(byte[] obj, jakarta.json.stream.JsonGenerator generator, SerializationContext ctx) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedContent = encoder.encode(obj);
        generator.write( new String(encodedContent));

    }



}
