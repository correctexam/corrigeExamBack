package fr.istic.service;

/**
 * Raised when an operation on the object storage (S3/MinIO or local files) fails.
 */
public class StorageException extends RuntimeException {

    private final String objectKey;

    public StorageException(String objectKey, String message, Throwable cause) {
        super(message + " (object: " + objectKey + ")", cause);
        this.objectKey = objectKey;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
