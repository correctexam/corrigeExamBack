package fr.istic.security;

/**
 * Constants JHipster authorities.
 */
public final class AuthoritiesConstants {
    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    /** Session of a student opened by the link sent to their e-mail address (ADR 0003): /api/student/me only. */
    public static final String STUDENT = "ROLE_STUDENT";

    private AuthoritiesConstants() {}
}
