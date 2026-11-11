package fr.istic.service.customdto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
@Schema(description = "Alignment progress/status event streamed to clients via Server-Sent Events (SSE).")
public class ScanStatusDto{

    /** Page processed by the worker. */
    public static final String STATUS_RUNNING = "running";
    /** Job completed: the cache cache/{examId}.sqlite3 is ready. */
    public static final String STATUS_FINISHED = "finished";
    /** Job failed: details contains the reason. */
    public static final String STATUS_ERROR = "error";
    /** Sent by the back-end as soon as a client subscribes: the job can be submitted safely. */
    public static final String STATUS_CONNECTED = "connected";

    @JsonProperty("exam_id")
    @Schema(description = "Identifier of the exam being aligned.")
    public int examId;
    @JsonProperty("page")
    @Schema(description = "Page this status event refers to.")
    public int page;
    @JsonProperty("status")
    @Schema(description = "connected (subscription ready), running (page processed), finished (job done) or error (job failed).")
    public String status;
    @JsonProperty("progress")
    @Schema(description = "Progress percentage of the page (0-100).")
    public int progress;
    @JsonProperty("details")
    @Schema(description = "Optional human-readable detail about the current stage.")
    public String details;

    public ScanStatusDto() {
    }

    public ScanStatusDto(int examId, int page, String status, int progress, String details) {
        this.examId = examId;
        this.page = page;
        this.status = status;
        this.progress = progress;
        this.details = details;
    }

    /** First event of every SSE subscription. */
    public static ScanStatusDto connected(int examId) {
        return new ScanStatusDto(examId, 0, STATUS_CONNECTED, 0, "subscribed");
    }
}
