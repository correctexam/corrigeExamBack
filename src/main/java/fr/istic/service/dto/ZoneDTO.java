package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Zone} entity.
 */
@RegisterForReflection
@Schema(description = "A rectangular region on a page of a template identifying where a value is read or written.")
public class ZoneDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Page number on which the zone is located.")
    public Integer pageNumber;

    @Schema(description = "Initial X coordinate of the zone.")
    public Integer xInit;

    @Schema(description = "Initial Y coordinate of the zone.")
    public Integer yInit;

    @Schema(description = "Width of the zone.")
    public Integer width;

    @Schema(description = "Height of the zone.")
    public Integer height;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ZoneDTO)) {
            return false;
        }

        return id != null && id.equals(((ZoneDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ZoneDTO{" +
            "id=" + id +
            ", pageNumber=" + pageNumber +
            ", xInit=" + xInit +
            ", yInit=" + yInit +
            ", width=" + width +
            ", height=" + height +
            "}";
    }
}
