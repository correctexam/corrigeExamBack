package fr.istic.web.rest.vm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Configuration of a single application logger.")
public class LoggerVM {
  @Schema(description = "Name of the logger.")
  public String name;
  @Schema(description = "Effective log level applied to the logger.")
  public String effectiveLevel;
  @Schema(description = "Configured log level of the logger.")
  public String configuredLevel;

  public LoggerVM(String name, Logger logger) {
    this.name = name;
    this.configuredLevel = Optional.of(logger).map(Logger::getLevel).map(Level::getName).orElse(null);
    this.effectiveLevel = retrieveEffectiveLogLevel(logger);
  }

  public LoggerVM(String name, String effectiveLevel, String configuredLevel) {
    this.name = name;
    this.effectiveLevel = effectiveLevel;
    this.configuredLevel = configuredLevel;
  }

  public LoggerVM() {}

  private String retrieveEffectiveLogLevel(Logger logger) {
    if (logger == null) {
      return null;
    }
    if (logger.getLevel() != null) {
      return logger.getLevel().getName();
    }

    return retrieveEffectiveLogLevel(logger.getParent());
  }
}
