package fr.istic.service.customdto.exportpdf;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Zonepdf {
    private long height;
    private long id;
    private long pageNumber;
    private long width;

    @JsonProperty("xInit")
    private long xInit;
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
