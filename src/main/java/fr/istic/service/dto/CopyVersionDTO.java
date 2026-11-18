package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Version of a copy of the scan (T25e6b): the copy starting at page {@code firstPage} (from 1) was printed in version
 * {@code label} (A–F); {@code edition} and {@code copy} come from its page codes, when known.
 */
@RegisterForReflection
public record CopyVersionDTO(Integer firstPage, String label, Long edition, Integer copy) {
}
