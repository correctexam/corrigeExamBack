package fr.istic.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Warns, at startup of the prod profile, about credentials still equal to the values written in the source
 * repository (application.properties, docker and k8s examples). They are public: every deployment should set its
 * own through environment variables (see docs/security/credentials.md). The application still starts.
 */
@ApplicationScoped
public class DefaultCredentialsCheck {

    /** Known values of the repository, by configuration key. */
    static final Map<String, String> REPOSITORY_DEFAULTS = new LinkedHashMap<>();

    static {
        REPOSITORY_DEFAULTS.put("quarkus.datasource.password", "test");
        REPOSITORY_DEFAULTS.put("quarkus.minio.access-key", "admin");
        REPOSITORY_DEFAULTS.put("quarkus.minio.secret-key", "minioadmin");
        REPOSITORY_DEFAULTS.put("rabbitmq-username", "rabbitmq");
        REPOSITORY_DEFAULTS.put("rabbitmq-password", "rabbitmq");
    }

    /** Environment variable that overrides each key (Quarkus naming rules). */
    static final Map<String, String> ENVIRONMENT_VARIABLES = Map.of(
        "quarkus.datasource.password", "DB_PASSWORD",
        "quarkus.minio.access-key", "QUARKUS_MINIO_ACCESS_KEY",
        "quarkus.minio.secret-key", "QUARKUS_MINIO_SECRET_KEY",
        "rabbitmq-username", "RABBITMQ_USERNAME",
        "rabbitmq-password", "RABBITMQ_PASSWORD"
    );

    private final Logger log = LoggerFactory.getLogger(DefaultCredentialsCheck.class);

    @Inject
    Config config;

    void onStart(@Observes StartupEvent event) {
        if (!ConfigUtils.getProfiles().contains("prod")) {
            return;
        }
        boolean usesS3 = config.getOptionalValue("correctexam.uses3", Boolean.class).orElse(false);
        Map<String, Optional<String>> values = new LinkedHashMap<>();
        for (String key : REPOSITORY_DEFAULTS.keySet()) {
            if (!usesS3 && key.startsWith("quarkus.minio.")) {
                continue;
            }
            values.put(key, config.getOptionalValue(key, String.class));
        }
        for (String key : defaultsInUse(values)) {
            log.error(
                "{} still has the value written in the source repository: set {} on this deployment " +
                "(see docs/security/credentials.md)",
                key,
                ENVIRONMENT_VARIABLES.get(key)
            );
        }
    }

    /** Keys whose value is the one of the repository (absent values are not reported). */
    static List<String> defaultsInUse(Map<String, Optional<String>> values) {
        List<String> found = new ArrayList<>();
        values.forEach((key, value) -> {
            String known = REPOSITORY_DEFAULTS.get(key);
            if (known != null && value.isPresent() && known.equals(value.get())) {
                found.add(key);
            }
        });
        return found;
    }
}
