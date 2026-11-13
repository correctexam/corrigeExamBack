package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
@Schema(description = "Number of pages of a scan PDF.")
public class ScanPageCountDto {

    @Schema(description = "Identifier of the scan.")
    public long scanId;

    @Schema(description = "Number of pages of the scan PDF.")
    public int pageCount;

    public ScanPageCountDto() {
    }

    public ScanPageCountDto(long scanId, int pageCount) {
        this.scanId = scanId;
        this.pageCount = pageCount;
    }
}
