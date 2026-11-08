package fr.istic.web.rest.vm;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Environment properties exposed by the /management/env endpoint.")
public class EnvVM {
  @Schema(description = "List of active application profiles.")
  public final Collection<String> activeProfiles;
  @Schema(description = "List of property sources with their properties.")
  public final Collection<PropertySource> propertySources;

  public EnvVM(Collection<String> activeProfiles, Collection<PropertySource> propertySources) {
    this.activeProfiles = activeProfiles;
    this.propertySources = propertySources;
  }

  @RegisterForReflection
  @Schema(description = "A named source of environment properties.")
  public static class PropertySource {
    @Schema(description = "Name of the property source.")
    public final String name;
    @Schema(description = "Map of property names to their property values.")
    public final Map<String, PropertyValue> properties;

    public PropertySource(String name, Map<String, String> properties) {
      this.name = name;
      this.properties = properties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, PropertyValue::new));
    }

    @RegisterForReflection
    @Schema(description = "Value of a single environment property.")
    public static class PropertyValue {
      @Schema(description = "Value of the property; sensitive values are obfuscated.")
      public final String value;

      public PropertyValue(Map.Entry<String, String> entry) {
        this.value = retrieveValue(entry);
      }

      public PropertyValue(String value) {
        this.value = value;
      }

      private String retrieveValue(Map.Entry<String, String> entry) {
        final String obfuscatedValue = "******";
        String[] prefixes = { "password", "credentials", "credential", "secret", "secrets" };

        for (String prefix : prefixes) {
        String key = entry.getKey();
        if (key != null && key.toLowerCase().contains(prefix)) {
            return obfuscatedValue;
          }
        }

        return entry.getValue();
      }
    }
  }
}
