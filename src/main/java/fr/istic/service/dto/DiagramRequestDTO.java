package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** A text diagram to render with Kroki (T25c): {@code kind} is the language of the Typst raw block. */
@RegisterForReflection
public record DiagramRequestDTO(String kind, String source) {
}
