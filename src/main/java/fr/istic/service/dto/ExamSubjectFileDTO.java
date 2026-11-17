package fr.istic.service.dto;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** A file of a Typst subject (T25c). */
@RegisterForReflection
public record ExamSubjectFileDTO(String path, long size, String sha256, String contentType, Instant updatedAt) {
}
