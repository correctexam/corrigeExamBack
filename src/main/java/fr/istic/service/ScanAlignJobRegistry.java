package fr.istic.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.service.customdto.ScanAlignJobDto;
import fr.istic.service.customdto.ScanRequestDto;
import fr.istic.service.customdto.ScanStatusDto;

/**
 * Last known state of the server-side alignment job of each exam.
 *
 * <p>Fed by the job submissions ({@code POST /api/scansalign}) and by the statuses of the
 * Rust worker relayed by {@link ScanStreamService}. It lets a client find a running job
 * again ({@code GET /api/scansalign/{examId}/status}) and prevents a second job from being
 * submitted for an exam whose job is still running (both would write
 * {@code cache/{examId}.sqlite3}).</p>
 *
 * <p>Limits, by design (see ADR 0001):</p>
 * <ul>
 *   <li>the state is kept in memory: after a restart, a running job is visible again as soon
 *   as the worker publishes its next status, but without its submission data;</li>
 *   <li>with several back-end instances, each instance only sees the statuses it consumes
 *   (queue {@code scan_status_java_listener}).</li>
 * </ul>
 *
 * <p>An active job without any status for {@code correctexam.scanalign.stale-after} is
 * considered lost (worker stopped, message lost) and reported in error, so that no client
 * waits forever. Finished and failed jobs are kept for {@code correctexam.scanalign.retention}
 * so that a client that subscribes just after the end still learns the outcome.</p>
 */
@ApplicationScoped
public class ScanAlignJobRegistry {

    static final String STALE_DETAILS = "No status received from the alignment worker";

    private final Logger log = LoggerFactory.getLogger(ScanAlignJobRegistry.class);

    private final ConcurrentMap<Long, ScanAlignJobDto> jobs = new ConcurrentHashMap<>();
    private final Duration staleAfter;
    private final Duration retention;
    private final Clock clock;

    @Inject
    public ScanAlignJobRegistry(
            @ConfigProperty(name = "correctexam.scanalign.stale-after", defaultValue = "PT15M") Duration staleAfter,
            @ConfigProperty(name = "correctexam.scanalign.retention", defaultValue = "PT1H") Duration retention) {
        this(staleAfter, retention, Clock.systemUTC());
    }

    ScanAlignJobRegistry(Duration staleAfter, Duration retention, Clock clock) {
        this.staleAfter = staleAfter;
        this.retention = retention;
        this.clock = clock;
    }

    /**
     * Records a new job for the exam of the request.
     *
     * @return the job already running for this exam if there is one (nothing is recorded),
     *         empty if the new job has been recorded and can be sent to the worker
     */
    public Optional<ScanAlignJobDto> submit(ScanRequestDto request) {
        Instant now = clock.instant();
        ScanAlignJobDto[] running = new ScanAlignJobDto[1];
        jobs.compute(request.examID, (id, current) -> {
            ScanAlignJobDto job = expire(current, now);
            if (job != null && job.isActive()) {
                running[0] = job.copy();
                return job;
            }
            ScanAlignJobDto submitted = new ScanAlignJobDto();
            submitted.examId = id;
            submitted.status = ScanAlignJobDto.STATUS_SUBMITTED;
            submitted.pagesToManage = request.pagesToManage;
            submitted.submittedAt = now;
            submitted.updatedAt = now;
            return submitted;
        });
        return Optional.ofNullable(running[0]);
    }

    /** Marks the job of the exam as failed (for instance when it could not be sent to RabbitMQ). */
    public void fail(long examId, String details) {
        Instant now = clock.instant();
        jobs.computeIfPresent(examId, (id, job) -> {
            job.status = ScanStatusDto.STATUS_ERROR;
            job.details = details;
            job.updatedAt = now;
            return job;
        });
    }

    /** Updates the job state with a status published by the worker. */
    public void onStatus(ScanStatusDto status) {
        if (status == null || status.status == null || ScanStatusDto.STATUS_CONNECTED.equals(status.status)) {
            return;
        }
        Instant now = clock.instant();
        jobs.compute((long) status.examId, (id, current) -> {
            ScanAlignJobDto job = current;
            if (job == null) {
                // job submitted before a restart or through another instance
                job = new ScanAlignJobDto();
                job.examId = id;
            }
            job.status = status.status;
            if (status.page > 0) {
                job.page = status.page;
            }
            job.progress = Math.max(0, Math.min(100, status.progress));
            job.details = status.details;
            job.updatedAt = now;
            return job;
        });
    }

    /** Last known state of the job of the exam, empty if there is none. */
    public Optional<ScanAlignJobDto> find(long examId) {
        Instant now = clock.instant();
        ScanAlignJobDto job = jobs.computeIfPresent(examId, (id, current) -> expire(current, now));
        return Optional.ofNullable(job).map(ScanAlignJobDto::copy);
    }

    /** Applies the staleness and retention rules; returns null if the job must be forgotten. */
    private ScanAlignJobDto expire(ScanAlignJobDto job, Instant now) {
        if (job == null) {
            return null;
        }
        if (job.isActive()) {
            if (job.updatedAt.plus(staleAfter).isBefore(now)) {
                log.warn("Alignment job of exam {} considered lost: no status since {}", job.examId, job.updatedAt);
                job.status = ScanStatusDto.STATUS_ERROR;
                job.details = STALE_DETAILS;
                job.updatedAt = now;
            }
            return job;
        }
        return job.updatedAt.plus(retention).isBefore(now) ? null : job;
    }
}
