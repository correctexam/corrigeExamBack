package fr.istic.service.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Version of an exam (T25e, ADR 0005): plan and layout as written by the front, and whether its PDF is in the storage.
 */
@RegisterForReflection
public record ExamVersionDTO(long id, long examId, int index, String label, long seed, JsonNode plan, JsonNode layout, int pages,
        boolean hasPdf, Long pdfSize, Instant createdAt, String createdBy) {
}
