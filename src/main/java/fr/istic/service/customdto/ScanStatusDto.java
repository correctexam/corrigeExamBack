package fr.istic.service.customdto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
@Schema(description = "Alignment progress/status event streamed to clients via Server-Sent Events (SSE).")
public class ScanStatusDto{
    @JsonProperty("exam_id")
    @Schema(description = "Identifier of the exam being aligned.")
    public int examId;
    @JsonProperty("page")
    @Schema(description = "Page this status event refers to.")
    public int page;
    @JsonProperty("status")
    @Schema(description = "Processing status of the page (e.g. PENDING, IN_PROGRESS, DONE).")
    public String status;
    @JsonProperty("progress")
    @Schema(description = "Progress percentage of the page (0-100).")
    public int progress;
    @JsonProperty("details")
    @Schema(description = "Optional human-readable detail about the current stage.")
    public String details;
}
