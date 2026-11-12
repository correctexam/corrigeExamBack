package fr.istic.service.customdto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Last known state of the server-side alignment job of an exam, as seen by this back-end
 * instance (see {@link fr.istic.service.ScanAlignJobRegistry}).
 *
 * <p>Lets a client that was not connected when the job was submitted (page reloaded,
 * browser closed, another tab or another teacher) find out that a job is running,
 * display its progress and follow it on the SSE stream.</p>
 */
@RegisterForReflection
@Schema(description = "Last known state of the server-side alignment job of an exam.")
public class ScanAlignJobDto {

    /** Job accepted by the back-end and queued: no status received from the worker yet. */
    public static final String STATUS_SUBMITTED = "submitted";

    @JsonProperty("exam_id")
    @Schema(description = "Identifier of the exam being aligned.")
    public long examId;

    @JsonProperty("status")
    @Schema(description = "submitted (queued), running, finished or error.")
    public String status;

    @JsonProperty("pages_to_manage")
    @Schema(description = "Pages requested, as sent in ScanRequestDto (e.g. '1-3; 5'). Null when the job was submitted through another back-end instance or before a restart.")
    public String pagesToManage;

    @JsonProperty("page")
    @Schema(description = "Last page processed by the worker (0 if none).")
    public int page;

    @JsonProperty("progress")
    @Schema(description = "Overall progress of the job (0-100).")
    public int progress;

    @JsonProperty("details")
    @Schema(description = "Last detail sent by the worker (error message for the error status).")
    public String details;

    @JsonProperty("submitted_at")
    @Schema(description = "Submission time, null when unknown.")
    public Instant submittedAt;

    @JsonProperty("updated_at")
    @Schema(description = "Time of the last status received for this job.")
    public Instant updatedAt;

    public ScanAlignJobDto() {
    }

    public boolean isActive() {
        return STATUS_SUBMITTED.equals(status) || ScanStatusDto.STATUS_RUNNING.equals(status);
    }

    public ScanAlignJobDto copy() {
        ScanAlignJobDto c = new ScanAlignJobDto();
        c.examId = examId;
        c.status = status;
        c.pagesToManage = pagesToManage;
        c.page = page;
        c.progress = progress;
        c.details = details;
        c.submittedAt = submittedAt;
        c.updatedAt = updatedAt;
        return c;
    }
}
