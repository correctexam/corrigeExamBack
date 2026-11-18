package fr.istic.service.subject;

/** A request on the versions of an exam that cannot be honoured (T25e); the resource maps the reason to a status. */
public class VersionException extends RuntimeException {

    public enum Reason {
        /** 404: no exam, no such version or no PDF for it. */
        NOT_FOUND,
        /** 400: versions, plan, layout or PDF not valid. */
        INVALID,
        /** 409: a print run of several versions exists, the versions can no longer change. */
        FROZEN,
        /** 413: PDF or JSON over the limits. */
        TOO_LARGE,
        /** 503: the object storage is not available. */
        STORAGE_UNAVAILABLE
    }

    private final Reason reason;

    public VersionException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public VersionException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
