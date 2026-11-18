package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Print run requested by the editor (T25d): number of copies and pages of the template; {@code versions} (T25e) is the
 * number of versions of the subject used, 1 when absent.
 */
@RegisterForReflection
public record EditionRequestDTO(Integer copies, Integer pagesPerCopy, Integer versions) {
}
