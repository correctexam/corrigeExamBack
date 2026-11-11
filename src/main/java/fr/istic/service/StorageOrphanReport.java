package fr.istic.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of a storage orphan analysis (and, when {@link #dryRun} is false, of the cleanup).
 */
public class StorageOrphanReport {

    /** Why an object is considered orphaned. */
    public enum Reason {
        /** {@code scan/{scanId}.pdf} whose Scan does not exist anymore. */
        SCAN_MISSING,
        /** {@code template/{templateId}.pdf|zip} whose Template does not exist anymore. */
        TEMPLATE_MISSING,
        /** {@code cache/{examId}...} or {@code exportpdf/{examId}/...} whose Exam does not exist anymore. */
        EXAM_MISSING
    }

    /** An orphaned object. */
    public record Orphan(String key, Reason reason, long ownerId, long size, Instant lastModified) {
    }

    public boolean dryRun = true;
    public long minAgeHours;
    public Instant analyzedAt;

    /** Number of objects listed in the storage. */
    public int scannedObjects;
    /** Objects whose owner still exists. */
    public int referencedObjects;
    /** Orphans younger than {@link #minAgeHours}: possibly an upload still in progress, never deleted. */
    public int skippedTooRecent;

    public final List<Orphan> orphans = new ArrayList<>();
    /** Keys that do not follow any known naming pattern: reported, never deleted. */
    public final List<String> unknownKeys = new ArrayList<>();

    /** Keys actually deleted (empty in dry-run mode). */
    public final List<String> deleted = new ArrayList<>();
    /** Keys whose deletion failed. */
    public final List<String> failed = new ArrayList<>();

    public long getOrphanBytes() {
        return orphans.stream().mapToLong(Orphan::size).sum();
    }

    public int getOrphanCount() {
        return orphans.size();
    }
}
