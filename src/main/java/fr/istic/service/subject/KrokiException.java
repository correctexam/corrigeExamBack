package fr.istic.service.subject;

/** A diagram could not be rendered by Kroki (T25c). */
public class KrokiException extends RuntimeException {

    public enum Reason {
        /** No Kroki URL configured: diagram rendering is disabled on this server. */
        NOT_CONFIGURED,
        /** Kroki did not answer (down, timeout, network). */
        UNAVAILABLE,
        /** Kroki refused the source (syntax error): the message is shown to the teacher. */
        INVALID_SOURCE,
        /** The source or the SVG exceeds the configured limits. */
        TOO_LARGE
    }

    private final Reason reason;

    public KrokiException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public KrokiException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
