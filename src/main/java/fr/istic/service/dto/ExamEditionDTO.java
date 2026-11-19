package fr.istic.service.dto;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Print run of an exam (T25d): copies 1 to {@code copies}, {@code pagesPerCopy} pages each; copy c is printed in
 * version (c - 1) mod {@code versions} (T25e).
 */
@RegisterForReflection
public record ExamEditionDTO(long id, long examId, int copies, int pagesPerCopy, int versions, Instant createdAt, String createdBy) {
}
