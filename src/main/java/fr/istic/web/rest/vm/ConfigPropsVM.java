package fr.istic.web.rest.vm;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Application configuration properties exposed by management endpoints.")
public class ConfigPropsVM {
  @Schema(description = "Map of configuration contexts exposed by the management endpoint.")
  public final Map<String, Context> contexts = new HashMap<>();

  public ConfigPropsVM() {
    this.contexts.put("JHipster", new Context());
  }

  @RegisterForReflection
  @Schema(description = "Configuration context holding the application beans.")
  public class Context {
    @Schema(description = "Map of bean names to their configuration values.")
    public final Map<String, Object> beans = new HashMap<>();
  }
}
