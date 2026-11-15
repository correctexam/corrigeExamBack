package fr.istic.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.istic.config.StudentPortalConfig;
import fr.istic.service.customdto.studentportal.AltchaChallengeDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * ALTCHA captcha (https://altcha.org, protocol v1): a proof of work solved by the browser, without third-party
 * service, cookie or tracking (ADR 0003).
 * <p>
 * Challenge: {@code challenge = SHA-256(salt + number)} for a random secret {@code number <= maxnumber}, signed by
 * {@code signature = HMAC-SHA256(challenge, key)}; the salt carries its expiry ({@code ...?expires=<epoch seconds>}).
 * The widget finds {@code number} and sends back, in base 64, the JSON
 * {@code {algorithm, challenge, number, salt, signature}}. A solved challenge is accepted once: the caller records it
 * (see {@link StudentPortalService}).
 */
@ApplicationScoped
public class AltchaService {

    private static final Logger log = LoggerFactory.getLogger(AltchaService.class);

    public static final String ALGORITHM = "SHA-256";
    private static final String HMAC = "HmacSHA256";
    private static final String EXPIRES = "?expires=";

    private final SecureRandom random = new SecureRandom();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final byte[] key;
    private final int maxNumber;
    private final long challengeTtlSeconds;
    private final Clock clock;

    @Inject
    public AltchaService(StudentPortalConfig config) {
        this(config.altchaHmacKey().filter(k -> !k.isBlank()).map(k -> k.getBytes(StandardCharsets.UTF_8)).orElseGet(() -> {
            log.warn("correctexam.student-portal.altcha-hmac-key is not set: random key for this instance only "
                    + "(set the same key on every instance in production)");
            byte[] k = new byte[32];
            new SecureRandom().nextBytes(k);
            return k;
        }), config.altchaMaxNumber(), config.altchaChallengeTtl().toSeconds(), Clock.systemUTC());
    }

    /** For the unit tests. */
    AltchaService(byte[] key, int maxNumber, long challengeTtlSeconds, Clock clock) {
        this.key = key.clone();
        this.maxNumber = maxNumber;
        this.challengeTtlSeconds = challengeTtlSeconds;
        this.clock = clock;
    }

    public AltchaChallengeDTO createChallenge() {
        byte[] saltBytes = new byte[12];
        random.nextBytes(saltBytes);
        String salt = HexFormat.of().formatHex(saltBytes) + EXPIRES + (clock.instant().getEpochSecond() + challengeTtlSeconds);
        int number = random.nextInt(maxNumber + 1);
        String challenge = sha256Hex(salt + number);
        AltchaChallengeDTO dto = new AltchaChallengeDTO();
        dto.algorithm = ALGORITHM;
        dto.challenge = challenge;
        dto.maxnumber = maxNumber;
        dto.salt = salt;
        dto.signature = hmacHex(challenge);
        return dto;
    }

    /**
     * Checks a solution sent by the widget.
     *
     * @return the solved challenge (to record so that it is not accepted twice), or empty when the solution is
     *         missing, malformed, wrong, not signed by this server or expired
     */
    public Optional<String> verify(String base64Payload) {
        if (base64Payload == null || base64Payload.isBlank() || base64Payload.length() > 2048) {
            return Optional.empty();
        }
        try {
            JsonNode payload = objectMapper.readTree(Base64.getDecoder().decode(base64Payload.trim()));
            String algorithm = payload.path("algorithm").asText("");
            String challenge = payload.path("challenge").asText("");
            String salt = payload.path("salt").asText("");
            String signature = payload.path("signature").asText("");
            JsonNode numberNode = payload.path("number");
            if (!ALGORITHM.equals(algorithm) || challenge.isEmpty() || salt.isEmpty() || !numberNode.canConvertToLong()) {
                return Optional.empty();
            }
            long number = numberNode.asLong();
            if (number < 0 || number > maxNumber || expired(salt)) {
                return Optional.empty();
            }
            boolean solved = constantTimeEquals(sha256Hex(salt + number), challenge);
            boolean signed = constantTimeEquals(hmacHex(challenge), signature);
            return solved && signed ? Optional.of(challenge) : Optional.empty();
        } catch (IllegalArgumentException | java.io.IOException e) {
            log.debug("Malformed ALTCHA payload: {}", e.toString());
            return Optional.empty();
        }
    }

    private boolean expired(String salt) {
        int i = salt.indexOf(EXPIRES);
        if (i < 0) {
            return true;
        }
        try {
            long expires = Long.parseLong(salt.substring(i + EXPIRES.length()).split("&")[0]);
            return Instant.ofEpochSecond(expires).isBefore(clock.instant());
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /** HMAC-SHA256 of a value with the server key, in hex: also used to pseudonymise e-mail and IP addresses. */
    public String hmacHex(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    public static String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(ALGORITHM).digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
