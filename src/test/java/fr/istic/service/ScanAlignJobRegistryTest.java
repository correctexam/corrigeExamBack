package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import fr.istic.service.customdto.ScanAlignJobDto;
import fr.istic.service.customdto.ScanRequestDto;
import fr.istic.service.customdto.ScanStatusDto;

/**
 * Unit tests of {@link ScanAlignJobRegistry}: job life cycle, refusal of a second job,
 * staleness and retention.
 */
class ScanAlignJobRegistryTest {

    /** Clock moved forward by the tests. */
    private static final class TestClock extends Clock {
        private Instant now = Instant.parse("2026-11-12T08:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final TestClock clock = new TestClock();
    private final ScanAlignJobRegistry registry = new ScanAlignJobRegistry(Duration.ofMinutes(15), Duration.ofHours(1), clock);

    private static ScanRequestDto request(long examId, String pages) {
        ScanRequestDto r = new ScanRequestDto();
        r.examID = examId;
        r.pagesToManage = pages;
        return r;
    }

    private static ScanStatusDto status(int examId, int page, String status, int progress) {
        return new ScanStatusDto(examId, page, status, progress, "d");
    }

    @Test
    void submittedThenRunningThenFinished() {
        assertTrue(registry.submit(request(5, "1-10")).isEmpty());
        ScanAlignJobDto job = registry.find(5).orElseThrow();
        assertEquals(ScanAlignJobDto.STATUS_SUBMITTED, job.status);
        assertEquals("1-10", job.pagesToManage);
        assertTrue(job.isActive());

        registry.onStatus(status(5, 4, ScanStatusDto.STATUS_RUNNING, 40));
        job = registry.find(5).orElseThrow();
        assertEquals(ScanStatusDto.STATUS_RUNNING, job.status);
        assertEquals(4, job.page);
        assertEquals(40, job.progress);
        assertEquals("1-10", job.pagesToManage);

        registry.onStatus(status(5, 0, ScanStatusDto.STATUS_FINISHED, 100));
        job = registry.find(5).orElseThrow();
        assertEquals(ScanStatusDto.STATUS_FINISHED, job.status);
        assertFalse(job.isActive());
        assertEquals(4, job.page, "the final status (page 0) keeps the last page");
    }

    @Test
    void secondJobRejectedWhileTheFirstIsActive() {
        registry.submit(request(5, "1-10"));
        registry.onStatus(status(5, 2, ScanStatusDto.STATUS_RUNNING, 20));

        var conflict = registry.submit(request(5, "1-3"));
        assertTrue(conflict.isPresent());
        assertEquals(20, conflict.get().progress);
        assertEquals("1-10", registry.find(5).orElseThrow().pagesToManage, "running job unchanged");

        assertTrue(registry.submit(request(6, "1")).isEmpty(), "other exams are independent");
    }

    @Test
    void newJobAcceptedAfterTheEndOfThePreviousOne() {
        registry.submit(request(5, "1-10"));
        registry.onStatus(status(5, 0, ScanStatusDto.STATUS_ERROR, 0));

        assertTrue(registry.submit(request(5, "1-10")).isEmpty());
        assertEquals(ScanAlignJobDto.STATUS_SUBMITTED, registry.find(5).orElseThrow().status);
    }

    @Test
    void statusOfAJobSubmittedElsewhereIsRecorded() {
        registry.onStatus(status(9, 1, ScanStatusDto.STATUS_RUNNING, 5));

        ScanAlignJobDto job = registry.find(9).orElseThrow();
        assertTrue(job.isActive());
        assertNull(job.pagesToManage);
        assertNull(job.submittedAt);
    }

    @Test
    void connectedEventsAreIgnored() {
        registry.onStatus(ScanStatusDto.connected(3));
        assertTrue(registry.find(3).isEmpty());
    }

    @Test
    void activeJobWithoutNewsIsReportedInError() {
        registry.submit(request(5, "1-10"));
        clock.advance(Duration.ofMinutes(10));
        assertTrue(registry.find(5).orElseThrow().isActive());

        clock.advance(Duration.ofMinutes(6));
        ScanAlignJobDto job = registry.find(5).orElseThrow();
        assertEquals(ScanStatusDto.STATUS_ERROR, job.status);
        assertEquals(ScanAlignJobRegistry.STALE_DETAILS, job.details);
        assertTrue(registry.submit(request(5, "1-10")).isEmpty(), "a lost job does not block a new one");
    }

    @Test
    void endedJobsAreForgottenAfterTheRetention() {
        registry.submit(request(5, "1"));
        registry.onStatus(status(5, 0, ScanStatusDto.STATUS_FINISHED, 100));

        clock.advance(Duration.ofMinutes(59));
        assertTrue(registry.find(5).isPresent());
        clock.advance(Duration.ofMinutes(2));
        assertTrue(registry.find(5).isEmpty());
    }

    @Test
    void sendFailureEndsTheJob() {
        registry.submit(request(5, "1"));
        registry.fail(5, "broker down");

        ScanAlignJobDto job = registry.find(5).orElseThrow();
        assertEquals(ScanStatusDto.STATUS_ERROR, job.status);
        assertEquals("broker down", job.details);
    }

    @Test
    void findReturnsACopy() {
        registry.submit(request(5, "1"));
        registry.find(5).orElseThrow().status = "tampered";
        assertEquals(ScanAlignJobDto.STATUS_SUBMITTED, registry.find(5).orElseThrow().status);
    }
}
