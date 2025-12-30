package fr.istic.config;


import jakarta.inject.Singleton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.quarkus.jackson.ObjectMapperCustomizer;


@Singleton
public class JsonConfiguration implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
         var module = new SimpleModule();
        module.addDeserializer(byte[].class, new CustomByteDeserializer());
        module.addSerializer(new CustomByteSerializer());
        objectMapper.registerModule(module);

         DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

        // On utilise le module officiel JavaTimeModule pour surcharger LocalDateTime
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Sérialisation (Java -> JSON String)
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));

        // Désérialisation (JSON String -> Java)
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        // Enregistrement du module
        objectMapper.registerModule(javaTimeModule);


        // IMPORTANT : Désactiver l'écriture des dates en timestamps (tableaux de nombres [2023, 10, 25...])
        // pour forcer l'utilisation des Strings formatées.
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL); // Skip nulls
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT); // Compact in prod

        // === Deserialization Settings ===
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    }

     static class CustomOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // figured out that this method is not called during deserialization
    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      String dateString = p.getText();
      try {
        return OffsetDateTime.parse(dateString);
      } catch (Exception e) {
        var localDateTime = LocalDateTime.parse(dateString, FORMATTER);
        return localDateTime.atOffset(ZoneOffset.systemDefault().getRules().getOffset(localDateTime));
      }
    }

  }


    static class CustomByteDeserializer extends JsonDeserializer<byte[]> {


    // figured out that this method is not called during deserialization
    public byte[] deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
       String text = p.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            // Utilisation du décodeur standard Java
            return Base64.getDecoder().decode(text);
        } catch (IllegalArgumentException e) {
            // En cas de Base64 invalide
            throw new IOException("Impossible de décoder le Base64", e);
        }
    }
    }

    static class CustomByteSerializer extends com.fasterxml.jackson.databind.ser.std.StdSerializer<byte[]> {

        public CustomByteSerializer(){
            super(byte[].class);
        }

    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      if (value == null) {
            gen.writeNull();
        } else {
            // Utilisation de l'encodeur standard Java
            String base64String = Base64.getEncoder().encodeToString(value);
            gen.writeString(base64String);
        }
    }

  }

}
