package fr.istic.service;

import java.time.Instant;

/**
 * An object of the storage (S3/MinIO bucket or local file when {@code correctexam.saveasfile=true}).
 *
 * @param key          object key, always with '/' separators (e.g. {@code scan/12.pdf})
 * @param lastModified last modification date, or {@code null} when unknown
 * @param size         size in bytes
 */
public record StorageObject(String key, Instant lastModified, long size) {
}
