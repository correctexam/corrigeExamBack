package fr.istic.config;

import java.time.Duration;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Student portal (ADR 0003): access to their sheets through a link sent to the student's e-mail address.
 */
@ConfigMapping(prefix = "correctexam.student-portal")
public interface StudentPortalConfig {

    /** Disabled: the /api/public/my endpoints answer 404 (e.g. standalone version without mail server). */
    @WithDefault("true")
    boolean enabled();

    /** Base URL of the front-end in the links sent by e-mail; jhipster.mail.base-url when empty. */
    Optional<String> baseUrl();

    /** Validity of the link sent by e-mail (single use). */
    @WithDefault("PT30M")
    Duration tokenTtl();

    /** Validity of the student session opened with that link (no refresh). */
    @WithDefault("PT2H")
    Duration sessionTtl();

    /** Window over which the link requests are counted. */
    @WithDefault("PT15M")
    Duration requestWindow();

    /** Link requests allowed per e-mail address and window. */
    @WithDefault("3")
    int maxRequestsPerEmail();

    /** Link requests allowed per client IP address and window. */
    @WithDefault("30")
    int maxRequestsPerIp();

    /**
     * Secret key that signs the ALTCHA challenges and pseudonymises the e-mail and IP addresses of the requests.
     * Must be set, and identical, on every instance in production; a random key is drawn at start-up otherwise.
     */
    Optional<String> altchaHmacKey();

    /** Highest number of the ALTCHA proof of work (work of the browser: about maxNumber / 2 hashes). */
    @WithDefault("100000")
    int altchaMaxNumber();

    /** Validity of an ALTCHA challenge. */
    @WithDefault("PT10M")
    Duration altchaChallengeTtl();
}
