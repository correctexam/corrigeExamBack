package fr.istic.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure (database- and storage-free) classification of storage objects against the
 * identifiers that still exist in the database.
 *
 * <p>Known key patterns:</p>
 * <ul>
 * <li>{@code scan/{scanId}.pdf}</li>
 * <li>{@code template/{templateId}.pdf} and {@code template/{templateId}.zip}</li>
 * <li>{@code cache/{examId}indexdb.json}, {@code cache/{examId}_exam_template_indexdb.json},
 * {@code cache/{examId}_part_{n}_indexdb.json}, {@code cache/{examId}.sqlite3}</li>
 * <li>{@code exportpdf/{examId}/...}</li>
 * </ul>
 * Any other key is reported as unknown and never deleted.
 */
public final class StorageOrphanDetector {

    /** Owner of a storage object, deduced from its key. */
    public record Owner(StorageOrphanReport.Reason reasonIfMissing, long id) {
    }

    private static final Pattern SCAN = Pattern.compile("^scan/(\\d+)\\.pdf$");
    private static final Pattern TEMPLATE = Pattern.compile("^template/(\\d+)\\.(pdf|zip)$");
    private static final Pattern CACHE = Pattern.compile(
            "^cache/(\\d+)(indexdb\\.json|_exam_template_indexdb\\.json|_part_\\d+_indexdb\\.json|\\.sqlite3)$");
    private static final Pattern EXPORT_PDF = Pattern.compile("^exportpdf/(\\d+)/.+$");

    private StorageOrphanDetector() {
    }

    /** Deduces the owner of an object from its key, or empty when the key is unknown. */
    public static Optional<Owner> ownerOf(String key) {
        if (key == null) {
            return Optional.empty();
        }
        Matcher m = SCAN.matcher(key);
        if (m.matches()) {
            return Optional.of(new Owner(StorageOrphanReport.Reason.SCAN_MISSING, Long.parseLong(m.group(1))));
        }
        m = TEMPLATE.matcher(key);
        if (m.matches()) {
            return Optional.of(new Owner(StorageOrphanReport.Reason.TEMPLATE_MISSING, Long.parseLong(m.group(1))));
        }
        m = CACHE.matcher(key);
        if (m.matches()) {
            return Optional.of(new Owner(StorageOrphanReport.Reason.EXAM_MISSING, Long.parseLong(m.group(1))));
        }
        m = EXPORT_PDF.matcher(key);
        if (m.matches()) {
            return Optional.of(new Owner(StorageOrphanReport.Reason.EXAM_MISSING, Long.parseLong(m.group(1))));
        }
        return Optional.empty();
    }

    /**
     * Classifies the objects.
     *
     * @param objects     the storage objects
     * @param scanIds     identifiers of the existing scans
     * @param templateIds identifiers of the existing templates
     * @param examIds     identifiers of the existing exams
     * @param now         reference date
     * @param minAge      orphans more recent than this are skipped (upload possibly in progress)
     * @return a report in dry-run mode
     */
    public static StorageOrphanReport detect(Collection<StorageObject> objects, Set<Long> scanIds,
            Set<Long> templateIds, Set<Long> examIds, Instant now, Duration minAge) {
        StorageOrphanReport report = new StorageOrphanReport();
        report.analyzedAt = now;
        report.minAgeHours = minAge.toHours();
        report.dryRun = true;
        Instant threshold = now.minus(minAge);

        for (StorageObject object : objects) {
            report.scannedObjects++;
            Optional<Owner> owner = ownerOf(object.key());
            if (owner.isEmpty()) {
                report.unknownKeys.add(object.key());
                continue;
            }
            if (ownerExists(owner.get(), scanIds, templateIds, examIds)) {
                report.referencedObjects++;
                continue;
            }
            // Unknown date: treated as recent, the object is kept (safe side)
            if (object.lastModified() == null || object.lastModified().isAfter(threshold)) {
                report.skippedTooRecent++;
                continue;
            }
            report.orphans.add(new StorageOrphanReport.Orphan(object.key(), owner.get().reasonIfMissing(),
                    owner.get().id(), object.size(), object.lastModified()));
        }
        return report;
    }

    private static boolean ownerExists(Owner owner, Set<Long> scanIds, Set<Long> templateIds, Set<Long> examIds) {
        switch (owner.reasonIfMissing()) {
            case SCAN_MISSING:
                return scanIds.contains(owner.id());
            case TEMPLATE_MISSING:
                return templateIds.contains(owner.id());
            case EXAM_MISSING:
                return examIds.contains(owner.id());
            default:
                return true;
        }
    }
}
