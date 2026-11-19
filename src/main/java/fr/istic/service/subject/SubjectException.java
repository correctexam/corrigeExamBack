package fr.istic.service.subject;

/** A request on a Typst subject that cannot be honoured (T25c); the resource maps the reason to a status. */
public class SubjectException extends RuntimeException {

    public enum Reason {
        /** 404: no exam, no subject or no such file. */
        NOT_FOUND,
        /** 400: path not allowed. */
        BAD_PATH,
        /** 409: another teacher holds the lock, or the caller does not hold it. */
        LOCKED,
        /** 409: the subject changed since the revision the editor knows. */
        REVISION_CONFLICT,
        /** 413: file, subject or number of files over the limits. */
        TOO_LARGE,
        /** 503: the object storage is not available. */
        STORAGE_UNAVAILABLE
    }

    private final Reason reason;

    public SubjectException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public SubjectException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
