package fr.istic.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.Scan;
import fr.istic.domain.Template;

/**
 * Detects, and optionally deletes, storage objects whose owner (scan, template or exam)
 * no longer exists in the database.
 *
 * <p>Safety rules: dry-run by default, unknown keys are never deleted, and orphans more recent
 * than {@code minAge} are skipped because their database row may not be committed yet.</p>
 */
@ApplicationScoped
public class StorageOrphanService {

    /** Prefixes owned by the application in the bucket. */
    static final List<String> PREFIXES = List.of("scan/", "template/", "cache/", "exportpdf/");

    /** Default minimum age of an orphan before it can be deleted. */
    public static final Duration DEFAULT_MIN_AGE = Duration.ofHours(24);

    private final Logger log = LoggerFactory.getLogger(StorageOrphanService.class);

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    StorageDeletionService storageDeletionService;

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    /**
     * Analyses the storage and, unless {@code dryRun}, deletes the orphans.
     *
     * @param dryRun when true (recommended first), nothing is deleted
     * @param minAge orphans more recent than this are kept
     */
    public StorageOrphanReport cleanOrphans(boolean dryRun, Duration minAge) {
        if (!uses3) {
            throw new IllegalStateException("Orphan cleanup is only available when correctexam.uses3=true");
        }
        if (minAge == null || minAge.isNegative()) {
            throw new IllegalArgumentException("minAge must be a positive duration");
        }

        // List the storage BEFORE reading the database: an object created in between has a
        // recent date and is skipped thanks to minAge.
        List<StorageObject> objects = new ArrayList<>();
        for (String prefix : PREFIXES) {
            objects.addAll(fichierS3Service.listObjects(prefix));
        }
        StorageOrphanReport report = detect(objects, minAge);
        report.dryRun = dryRun;

        log.info("Storage orphan analysis: {} objects, {} orphans ({} bytes), {} too recent, {} unknown keys",
                report.scannedObjects, report.getOrphanCount(), report.getOrphanBytes(), report.skippedTooRecent,
                report.unknownKeys.size());

        if (!dryRun && !report.orphans.isEmpty()) {
            List<String> keys = report.orphans.stream().map(StorageOrphanReport.Orphan::key).toList();
            List<String> failures = storageDeletionService.deleteNow(keys);
            report.failed.addAll(failures);
            keys.stream().filter(k -> !failures.contains(k)).forEach(report.deleted::add);
            log.warn("Storage orphan cleanup: {} objects deleted, {} failures", report.deleted.size(),
                    report.failed.size());
        }
        return report;
    }

    /** Reads the existing identifiers and classifies the objects (read-only transaction). */
    @Transactional
    public StorageOrphanReport detect(List<StorageObject> objects, Duration minAge) {
        return StorageOrphanDetector.detect(objects, ids(Scan.class), ids(Template.class), ids(Exam.class),
                Instant.now(), minAge);
    }

    private Set<Long> ids(Class<?> entity) {
        List<Long> ids = Exam.getEntityManager()
                .createQuery("select e.id from " + entity.getSimpleName() + " e", Long.class)
                .getResultList();
        return new HashSet<>(ids);
    }
}
