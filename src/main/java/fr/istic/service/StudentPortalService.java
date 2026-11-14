package fr.istic.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.config.JHipsterProperties;
import fr.istic.config.StudentPortalConfig;
import fr.istic.domain.Student;
import fr.istic.domain.StudentAccessToken;
import fr.istic.domain.StudentPortalRequest;
import fr.istic.security.jwt.TokenProvider;
import fr.istic.service.customdto.studentportal.StudentSessionDTO;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Link to the student portal sent to the e-mail address of a student, and opening of the student session (ADR 0003).
 * <p>
 * The answer to a link request never tells whether the address is known: the e-mail is sent asynchronously, only when
 * a student record carries the address. Requests are limited per address and per IP, and each solved captcha is
 * accepted once.
 */
@ApplicationScoped
public class StudentPortalService {

    private static final Logger log = LoggerFactory.getLogger(StudentPortalService.class);

    /** Simple check of the shape of an address (the e-mail itself is the real check). */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MAX_EMAIL_LENGTH = 254;
    /** Requests and used or expired tokens are kept one day, then purged. */
    private static final Duration RETENTION = Duration.ofDays(1);

    public enum LinkOutcome {
        /** Accepted: an e-mail is sent if, and only if, the address is known (never told to the caller). */
        ACCEPTED,
        INVALID_EMAIL,
        INVALID_CAPTCHA,
        TOO_MANY_REQUESTS,
        DISABLED
    }

    private final SecureRandom random = new SecureRandom();

    @Inject
    StudentPortalConfig config;

    @Inject
    JHipsterProperties jHipsterProperties;

    @Inject
    AltchaService altchaService;

    @Inject
    TokenProvider tokenProvider;

    @Inject
    MailService mailService;

    /** Trimmed and lower-case address, the form used to match the student records. */
    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean isValidEmail(String normalizedEmail) {
        return !normalizedEmail.isEmpty() && normalizedEmail.length() <= MAX_EMAIL_LENGTH && EMAIL.matcher(normalizedEmail).matches();
    }

    private record Decision(LinkOutcome outcome, String token) {
    }

    public LinkOutcome requestLink(String rawEmail, String altchaPayload, String clientIp, String lang) {
        if (!config.enabled()) {
            return LinkOutcome.DISABLED;
        }
        String email = normalizeEmail(rawEmail);
        if (!isValidEmail(email)) {
            return LinkOutcome.INVALID_EMAIL;
        }
        Optional<String> challenge = altchaService.verify(altchaPayload);
        if (challenge.isEmpty()) {
            return LinkOutcome.INVALID_CAPTCHA;
        }

        Instant now = Instant.now();
        String emailHash = altchaService.hmacHex("email:" + email);
        String ipHash = altchaService.hmacHex("ip:" + (clientIp == null ? "" : clientIp));
        String challengeHash = AltchaService.sha256Hex(challenge.get());

        Decision decision;
        try {
            decision = QuarkusTransaction.requiringNew().call(() -> decide(email, emailHash, ipHash, challengeHash, now));
        } catch (RuntimeException e) {
            // The same solution sent twice at the same time: the unique challenge_hash refuses the second one
            log.debug("Student portal request refused: {}", e.toString());
            return LinkOutcome.INVALID_CAPTCHA;
        }
        if (decision.token() != null) {
            sendLink(email, decision.token(), lang);
        }
        return decision.outcome();
    }

    private Decision decide(String email, String emailHash, String ipHash, String challengeHash, Instant now) {
        StudentPortalRequest.deleteBefore(now.minus(RETENTION));
        StudentAccessToken.deleteExpiredBefore(now.minus(RETENTION));
        if (StudentPortalRequest.challengeUsed(challengeHash)) {
            return new Decision(LinkOutcome.INVALID_CAPTCHA, null);
        }
        Instant since = now.minus(config.requestWindow());
        boolean tooMany = StudentPortalRequest.countByEmailSince(emailHash, since) >= config.maxRequestsPerEmail()
                || StudentPortalRequest.countByIpSince(ipHash, since) >= config.maxRequestsPerIp();

        StudentPortalRequest request = new StudentPortalRequest();
        request.emailHash = emailHash;
        request.ipHash = ipHash;
        request.challengeHash = challengeHash;
        request.createdAt = now;
        request.persist();

        if (tooMany) {
            return new Decision(LinkOutcome.TOO_MANY_REQUESTS, null);
        }
        if (Student.count("lower(trim(mail)) = ?1", email) == 0) {
            // Unknown address: same answer, no e-mail
            return new Decision(LinkOutcome.ACCEPTED, null);
        }
        String value = newToken();
        StudentAccessToken accessToken = new StudentAccessToken();
        accessToken.email = email;
        accessToken.tokenHash = AltchaService.sha256Hex(value);
        accessToken.createdAt = now;
        accessToken.expiresAt = now.plus(config.tokenTtl());
        accessToken.persist();
        return new Decision(LinkOutcome.ACCEPTED, value);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void sendLink(String email, String token, String lang) {
        String baseUrl = config.baseUrl().filter(u -> !u.isBlank()).orElse(jHipsterProperties.mail().baseUrl());
        String link = baseUrl.replaceAll("/+$", "") + "/my/" + token;
        mailService.sendStudentPortalLink(email, link, config.tokenTtl().toMinutes(), lang)
            .exceptionally(e -> {
                log.error("Could not send a student portal link: {}", e.toString());
                return null;
            });
    }

    /**
     * Opens a student session with the token of a link: the token is used once, before its expiry.
     *
     * @return the session, or empty when the token is unknown, already used or expired
     */
    public Optional<StudentSessionDTO> openSession(String token) {
        if (!config.enabled() || token == null || token.isBlank() || token.length() > 200) {
            return Optional.empty();
        }
        String tokenHash = AltchaService.sha256Hex(token.trim());
        Instant now = Instant.now();
        Optional<String> email = QuarkusTransaction.requiringNew().call(() -> {
            // Conditional update: two concurrent uses of the same link cannot both succeed
            int used = StudentAccessToken.update("usedAt = ?1 where tokenHash = ?2 and usedAt is null and expiresAt > ?1", now, tokenHash);
            return used == 1 ? StudentAccessToken.findByTokenHash(tokenHash).map(t -> t.email) : Optional.<String>empty();
        });
        Instant expiresAt = now.plus(config.sessionTtl());
        return email.map(e -> new StudentSessionDTO(tokenProvider.createStudentToken(e, expiresAt), expiresAt, e));
    }
}
