package fr.istic.config;

import java.util.Base64;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;


public class FooSerializer implements JsonbSerializer<byte[]> {

    @Override
    public void serialize(byte[] obj, javax.json.stream.JsonGenerator generator, SerializationContext ctx) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedContent = encoder.encode(obj);
        generator.write( new String(encodedContent));

    }



}
