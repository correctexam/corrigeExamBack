package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import fr.istic.service.customdto.studentportal.AltchaChallengeDTO;

/** {@link AltchaService}: ALTCHA v1 challenges, as solved by the widget. No Quarkus boot. */
class AltchaServiceTest {

    private static final byte[] KEY = "unit-test-key".getBytes(StandardCharsets.UTF_8);
    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");

    private static AltchaService service(Instant now) {
        return new AltchaService(KEY, 500, 600, Clock.fixed(now, ZoneOffset.UTC));
    }

    /** What the widget does: find the number, then send the JSON in base 64. */
    static String solve(AltchaChallengeDTO c) {
        for (int n = 0; n <= c.maxnumber; n++) {
            if (AltchaService.sha256Hex(c.salt + n).equals(c.challenge)) {
                return payload(c, n, c.signature);
            }
        }
        throw new IllegalStateException("unsolvable challenge");
    }

    static String payload(AltchaChallengeDTO c, long number, String signature) {
        String json = "{\"algorithm\":\"" + c.algorithm + "\",\"challenge\":\"" + c.challenge + "\",\"number\":" + number
                + ",\"salt\":\"" + c.salt + "\",\"signature\":\"" + signature + "\"}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void acceptsASolvedChallenge() {
        AltchaService altcha = service(NOW);
        AltchaChallengeDTO c = altcha.createChallenge();
        assertEquals("SHA-256", c.algorithm);
        assertTrue(c.salt.contains("?expires="));
        assertEquals(c.challenge, altcha.verify(solve(c)).orElseThrow());
    }

    @Test
    void refusesAWrongNumberOrSignature() {
        AltchaService altcha = service(NOW);
        AltchaChallengeDTO c = altcha.createChallenge();
        int right = -1;
        for (int n = 0; n <= c.maxnumber; n++) {
            if (AltchaService.sha256Hex(c.salt + n).equals(c.challenge)) {
                right = n;
            }
        }
        assertTrue(altcha.verify(payload(c, right == 0 ? 1 : right - 1, c.signature)).isEmpty());
        String forged = (c.signature.charAt(0) == '0' ? "1" : "0") + c.signature.substring(1);
        assertTrue(altcha.verify(payload(c, right, forged)).isEmpty());
    }

    @Test
    void refusesAChallengeOfAnotherServer() {
        AltchaChallengeDTO c = new AltchaService("other-key".getBytes(StandardCharsets.UTF_8), 500, 600,
                Clock.fixed(NOW, ZoneOffset.UTC)).createChallenge();
        assertTrue(service(NOW).verify(solve(c)).isEmpty());
    }

    @Test
    void refusesAnExpiredChallenge() {
        AltchaChallengeDTO c = service(NOW).createChallenge();
        String solution = solve(c);
        assertTrue(service(NOW.plusSeconds(601)).verify(solution).isEmpty());
        assertTrue(service(NOW.plusSeconds(599)).verify(solution).isPresent());
    }

    @Test
    void refusesMalformedPayloads() {
        AltchaService altcha = service(NOW);
        assertTrue(altcha.verify(null).isEmpty());
        assertTrue(altcha.verify("").isEmpty());
        assertTrue(altcha.verify("not base 64 !").isEmpty());
        assertTrue(altcha.verify(Base64.getEncoder().encodeToString("{}".getBytes(StandardCharsets.UTF_8))).isEmpty());
    }
}
