package fr.istic.service.dto;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** Typst subject of an exam (T25c): its files and edition lock. The contents are read file by file. */
@RegisterForReflection
public record ExamSubjectDTO(long examId, String mainFile, int revision, Instant updatedAt, String updatedBy,
        List<ExamSubjectFileDTO> files, SubjectLockDTO lock) {
}
