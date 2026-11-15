package fr.istic.domain;

import java.time.Instant;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Link sent to a student to open the student portal (ADR 0003). Only the SHA-256 of the token is stored; the token is
 * used once, before {@link #expiresAt}.
 */
@Entity
@Table(name = "student_access_token")
@RegisterForReflection
public class StudentAccessToken extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** E-mail address the link was sent to (normalised: trimmed, lower case). */
    @Column(name = "email", nullable = false, length = 254)
    public String email;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    public String tokenHash;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    @Column(name = "used_at")
    public Instant usedAt;

    public static Optional<StudentAccessToken> findByTokenHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResultOptional();
    }

    public static long deleteExpiredBefore(Instant instant) {
        return delete("expiresAt < ?1", instant);
    }
}
