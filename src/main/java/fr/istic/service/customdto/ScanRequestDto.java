package fr.istic.service.customdto;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

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
    @JsonProperty("page_codes")
    @Schema(description = "Print runs of the exam (T25d3, ADR 0004), filled by the server from exam_edition: when not "
            + "empty and the job covers the whole scan, the worker reads the page codes CE1|print run|copy|page and "
            + "processes the pages in print order. A value sent by the client is replaced.", readOnly = true)
    public List<PageCodeEdition> pageCodes = new ArrayList<>();
    @JsonProperty("versions")
    @Schema(description = "Versions of the exam (T25e, ADR 0005), filled by the server from exam_version when a print "
            + "run has several versions: layout of each version, used by the worker to rebuild the copies of versions "
            + "B, C… in the layout of version A. A value sent by the client is replaced.", readOnly = true)
    public List<VersionLayout> versions = new ArrayList<>();

    /** Print run as the worker reads it ({@code EditionInfo} of the Rust worker). */
    @RegisterForReflection
    public static class PageCodeEdition {
        @JsonProperty("id")
        public long id;
        @JsonProperty("copies")
        public int copies;
        @JsonProperty("pages_per_copy")
        public int pagesPerCopy;
        /** Versions of the print run (T25e): copy c is printed in version (c - 1) mod versions. */
        @JsonProperty("versions")
        public int versions = 1;

        public PageCodeEdition() {
        }

        public PageCodeEdition(long id, int copies, int pagesPerCopy) {
            this(id, copies, pagesPerCopy, 1);
        }

        public PageCodeEdition(long id, int copies, int pagesPerCopy, int versions) {
            this.id = id;
            this.copies = copies;
            this.pagesPerCopy = pagesPerCopy;
            this.versions = versions;
        }
    }

    /** Layout of a version as the worker reads it ({@code ExamVersion} of the Rust worker, layout of format 1). */
    @RegisterForReflection
    public static class VersionLayout {
        @JsonProperty("index")
        public int index;
        @JsonProperty("label")
        public String label;
        @JsonProperty("layout")
        public JsonNode layout;

        public VersionLayout() {
        }

        public VersionLayout(int index, String label, JsonNode layout) {
            this.index = index;
            this.label = label;
            this.layout = layout;
        }
    }

}
