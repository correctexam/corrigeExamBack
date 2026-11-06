package fr.istic.service.customdto;


import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
@Schema(description = "Request payload to launch a server-side scan alignment job (consumed by the RabbitMQ alignment worker).")
public class ScanRequestDto{

    @JsonProperty("pages_to_manage")
    @Schema(description = "Pages to align, as contiguous ranges separated by ';' (e.g. '1-3; 5').", required = true)
    public String pagesToManage;
    @JsonProperty("exam_id")
    @Schema(description = "Identifier of the exam being aligned.", required = true)
    public long examID;
    @JsonProperty("template_id")
    @Schema(description = "Identifier of the template used for alignment.", required = true)
    public long templateID;
    @JsonProperty("scan_id")
    @Schema(description = "Identifier of the scanned file.", required = true)
    public long scanID;
    @JsonProperty("algo")
    @Schema(description = "Alignment algorithm: 0=NoAlign, 1=CircleAlign (with markers), 2=Akaze (without markers).", required = true)
    public Long algo;
    @JsonProperty("heightresolution")
    @Schema(description = "Target height resolution of aligned pages.")
    public Long heightresolution;
    @JsonProperty("corner_square_size")
    @Schema(description = "Size of the marker corner squares used by CircleAlign.")
    public Long corner_square_size;
    @JsonProperty("min_radius")
    @Schema(description = "Minimum marker radius used by CircleAlign.")
    public Float min_radius;

}
