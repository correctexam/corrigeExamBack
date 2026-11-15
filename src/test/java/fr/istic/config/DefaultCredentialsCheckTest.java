package fr.istic.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class DefaultCredentialsCheckTest {

    @Test
    void reportsTheValuesOfTheRepositoryOnly() {
        Map<String, Optional<String>> values = new LinkedHashMap<>();
        values.put("quarkus.datasource.password", Optional.of("test"));
        values.put("quarkus.minio.access-key", Optional.of("prod-access"));
        values.put("quarkus.minio.secret-key", Optional.of("minioadmin"));
        values.put("rabbitmq-username", Optional.empty());
        values.put("rabbitmq-password", Optional.of("Rabbitmq"));

        assertEquals(List.of("quarkus.datasource.password", "quarkus.minio.secret-key"), DefaultCredentialsCheck.defaultsInUse(values));
    }

    @Test
    void everyCheckedKeyNamesItsEnvironmentVariable() {
        assertEquals(DefaultCredentialsCheck.REPOSITORY_DEFAULTS.keySet(), DefaultCredentialsCheck.ENVIRONMENT_VARIABLES.keySet());
    }
}
