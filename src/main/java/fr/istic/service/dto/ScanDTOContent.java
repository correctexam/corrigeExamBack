package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Lob;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Scan} entity.
 */
@RegisterForReflection
@Schema(description = "A scanned file including its binary content.")
public class ScanDTOContent implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the scan.", required=true)
    public String name;

    @Lob
    @Schema(description = "Binary content of the scan file.")
    public byte[] content;

    @Schema(description = "MIME content type of the scan file.")
    public String contentContentType;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScanDTO)) {
            return false;
        }

        return id != null && id.equals(((ScanDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ScanDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", content='" + content + "'" +
            "}";
    }
}
