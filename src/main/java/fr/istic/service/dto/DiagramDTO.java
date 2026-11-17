package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** A rendered diagram (T25c), read with {@code GET /api/subject-diagrams/{kind}/{sha256}.svg}. */
@RegisterForReflection
public record DiagramDTO(String kind, String sha256) {
}
