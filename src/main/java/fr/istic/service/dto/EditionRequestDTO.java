package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** Print run requested by the editor (T25d): number of copies and pages of the template. */
@RegisterForReflection
public record EditionRequestDTO(Integer copies, Integer pagesPerCopy) {
}
