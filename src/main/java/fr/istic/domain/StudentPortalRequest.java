package fr.istic.domain;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A request of a link to the student portal (ADR 0003), kept for a day to limit the requests per address and to refuse
 * a solved captcha twice. E-mail and IP addresses are pseudonymised (HMAC with the server key).
 */
@Entity
@Table(name = "student_portal_request")
@RegisterForReflection
public class StudentPortalRequest extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "email_hash", nullable = false, length = 64)
    public String emailHash;

    @Column(name = "ip_hash", nullable = false, length = 64)
    public String ipHash;

    /** SHA-256 of the solved ALTCHA challenge: unique, so a solution is accepted once. */
    @Column(name = "challenge_hash", nullable = false, unique = true, length = 64)
    public String challengeHash;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    public static long countByEmailSince(String emailHash, Instant since) {
        return count("emailHash = ?1 and createdAt >= ?2", emailHash, since);
    }

    public static long countByIpSince(String ipHash, Instant since) {
        return count("ipHash = ?1 and createdAt >= ?2", ipHash, since);
    }

    public static boolean challengeUsed(String challengeHash) {
        return count("challengeHash", challengeHash) > 0;
    }

    public static long deleteBefore(Instant instant) {
        return delete("createdAt < ?1", instant);
    }
}
