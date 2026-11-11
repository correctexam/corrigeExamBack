package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link StorageOrphanDetector}: key patterns, owner lookup and safety rules.
 */
class StorageOrphanDetectorTest {

    private static final Instant NOW = Instant.parse("2026-11-11T12:00:00Z");
    private static final Instant OLD = NOW.minus(Duration.ofDays(3));
    private static final Duration MIN_AGE = Duration.ofHours(24);

    private static StorageObject old(String key) {
        return new StorageObject(key, OLD, 100);
    }

    @Test
    void recognisesEveryKnownKeyPattern() {
        assertOwner("scan/12.pdf", StorageOrphanReport.Reason.SCAN_MISSING, 12);
        assertOwner("template/7.pdf", StorageOrphanReport.Reason.TEMPLATE_MISSING, 7);
        assertOwner("template/7.zip", StorageOrphanReport.Reason.TEMPLATE_MISSING, 7);
        assertOwner("cache/42indexdb.json", StorageOrphanReport.Reason.EXAM_MISSING, 42);
        assertOwner("cache/42_exam_template_indexdb.json", StorageOrphanReport.Reason.EXAM_MISSING, 42);
        assertOwner("cache/42_part_3_indexdb.json", StorageOrphanReport.Reason.EXAM_MISSING, 42);
        assertOwner("cache/42.sqlite3", StorageOrphanReport.Reason.EXAM_MISSING, 42);
        assertOwner("exportpdf/42/DUPONT_Jean.pdf", StorageOrphanReport.Reason.EXAM_MISSING, 42);
    }

    @Test
    void unknownKeysHaveNoOwner() {
        for (String key : List.of("scan/abc.pdf", "scan/12.png", "template/7.docx", "cache/notes.json",
                "cache/42.sqlite3-journal", "exportpdf/42", "other/1.pdf", "scan/", "")) {
            assertTrue(StorageOrphanDetector.ownerOf(key).isEmpty(), key);
        }
    }

    @Test
    void reportsOnlyObjectsWhoseOwnerIsMissing() {
        List<StorageObject> objects = List.of(
                old("scan/1.pdf"), old("scan/2.pdf"),
                old("template/5.pdf"), old("template/6.zip"),
                old("cache/10.sqlite3"), old("cache/11_part_1_indexdb.json"),
                old("exportpdf/11/a.pdf"));

        StorageOrphanReport report = StorageOrphanDetector.detect(objects, Set.of(1L), Set.of(5L), Set.of(10L),
                NOW, MIN_AGE);

        assertEquals(7, report.scannedObjects);
        assertEquals(3, report.referencedObjects);
        assertEquals(List.of("scan/2.pdf", "template/6.zip", "cache/11_part_1_indexdb.json", "exportpdf/11/a.pdf"),
                report.orphans.stream().map(StorageOrphanReport.Orphan::key).toList());
        assertEquals(400, report.getOrphanBytes());
        assertTrue(report.dryRun);
    }

    @Test
    void scanAndTemplateIdsAreNotConfusedWithExamIds() {
        // scan 10 does not exist even though exam 10 does
        StorageOrphanReport report = StorageOrphanDetector.detect(List.of(old("scan/10.pdf")), Set.of(), Set.of(),
                Set.of(10L), NOW, MIN_AGE);

        assertEquals(1, report.getOrphanCount());
        assertEquals(StorageOrphanReport.Reason.SCAN_MISSING, report.orphans.get(0).reason());
    }

    @Test
    void recentOrUndatedOrphansAreSkipped() {
        List<StorageObject> objects = List.of(
                new StorageObject("scan/1.pdf", NOW.minus(Duration.ofHours(2)), 1),
                new StorageObject("scan/2.pdf", null, 1),
                new StorageObject("scan/3.pdf", NOW.minus(Duration.ofHours(25)), 1));

        StorageOrphanReport report = StorageOrphanDetector.detect(objects, Set.of(), Set.of(), Set.of(), NOW,
                MIN_AGE);

        assertEquals(2, report.skippedTooRecent);
        assertEquals(List.of("scan/3.pdf"), report.orphans.stream().map(StorageOrphanReport.Orphan::key).toList());
    }

    @Test
    void unknownKeysAreReportedButNeverOrphans() {
        StorageOrphanReport report = StorageOrphanDetector.detect(List.of(old("cache/manual-backup.json")),
                Set.of(), Set.of(), Set.of(), NOW, MIN_AGE);

        assertEquals(List.of("cache/manual-backup.json"), report.unknownKeys);
        assertEquals(0, report.getOrphanCount());
    }

    private static void assertOwner(String key, StorageOrphanReport.Reason reason, long id) {
        StorageOrphanDetector.Owner owner = StorageOrphanDetector.ownerOf(key).orElseThrow();
        assertEquals(reason, owner.reasonIfMissing(), key);
        assertEquals(id, owner.id(), key);
    }
}
