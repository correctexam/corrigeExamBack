package fr.istic.service.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Versions of an exam sent by the front (T25e, ADR 0005), replacing the previous ones: version {@code i} has index
 * {@code i} and label A, B… The PDFs are sent afterwards, one per version.
 */
@RegisterForReflection
public record ExamVersionsRequestDTO(Long seed, List<Version> versions) {

    /** One version: {@code plan} is the {@code ce-plan} input of the package, {@code layout} its layout (format 1). */
    @RegisterForReflection
    public record Version(Integer index, String label, JsonNode plan, JsonNode layout, Integer pages) {
    }
}
