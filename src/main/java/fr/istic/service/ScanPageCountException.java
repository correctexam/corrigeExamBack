package fr.istic.service;

/**
 * Raised when the number of pages of a scan cannot be returned.
 */
public class ScanPageCountException extends RuntimeException {

    public enum Reason {
        /** No scan with this id. */
        SCAN_NOT_FOUND,
        /** The scan exists but its PDF has not been uploaded. */
        FILE_NOT_FOUND,
        /** The stored file is not a readable PDF (corrupted, protected by a user password...). */
        INVALID_PDF,
        /** The object storage could not be read, or too many counts are already running. */
        UNAVAILABLE
    }

    private final Reason reason;

    public ScanPageCountException(Reason reason, String message) {
        this(reason, message, null);
    }

    public ScanPageCountException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
