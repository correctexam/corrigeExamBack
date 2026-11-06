package fr.istic.web.rest.vm;

import io.quarkus.panache.common.Page;

import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@Schema(description = "Pagination parameters (page index and page size).")

public class PageRequestVM {

    @QueryParam("page")
    @DefaultValue("0")
    @Positive
    @Schema(description = "Zero-based page index.", defaultValue="0")
    public  int index;

    @QueryParam("size")
    @DefaultValue("500")
    @Positive
    @Schema(description = "Number of items per page.", defaultValue="500")
    public int size;

    public Page toPage() {
        return Page.of(index, size);
    }

    @Override
    public String toString() {
        return "PageRequestVM{" +
            "page=" + index +
            ", size=" + size +
            '}';
    }
}
