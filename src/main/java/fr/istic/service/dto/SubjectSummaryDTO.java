package fr.istic.service.dto;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** A subject the user can reuse in another exam (T25c). */
@RegisterForReflection
public record SubjectSummaryDTO(long examId, String examName, Long courseId, String courseName, Instant updatedAt) {
}
