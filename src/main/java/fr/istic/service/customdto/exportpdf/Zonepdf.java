package fr.istic.service.customdto.exportpdf;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A zone printed in the exported PDF.")
public class Zonepdf {
    @Schema(description = "Height of the zone.")
    private long height;
    @Schema(description = "Identifier of the zone.")
    private long id;
    @Schema(description = "Page number of the zone.")
    private long pageNumber;
    @Schema(description = "Width of the zone.")
    private long width;

    @JsonProperty("xInit")
    @Schema(description = "Initial X coordinate of the zone.")
    private long xInit;
    @Schema(description = "Initial Y coordinate of the zone.")
    private long yInit;

    public long getHeight() { return height; }
    public void setHeight(long value) { this.height = value; }

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }

    public long getPageNumber() { return pageNumber; }
    public void setPageNumber(long value) { this.pageNumber = value; }

    public long getWidth() { return width; }
    public void setWidth(long value) { this.width = value; }

    @JsonProperty("xInit")
    public long getXInit() { return xInit; }
    @JsonProperty("xInit")
    public void setXInit(long value) { this.xInit = value; }

    @JsonProperty("yInit")
    public long getYInit() { return yInit; }
    @JsonProperty("yInit")
    public void setYInit(long value) { this.yInit = value; }
}
