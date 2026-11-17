package fr.istic.service.dto;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Edition lock of a subject (T25c): {@code held} when a teacher holds a valid lock, {@code mine} when it is the
 * caller.
 */
@RegisterForReflection
public record SubjectLockDTO(boolean held, boolean mine, String lockedBy, Instant expiresAt) {
}
