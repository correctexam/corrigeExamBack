package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.ExamSheet} entity.
 */
@RegisterForReflection
@Schema(description = "A sheet of a scanned exam covering a page range.")
public class ExamSheetDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the sheet.", required=true)
    public String name;

    @Schema(description = "First page of the sheet.")
    public Integer pagemin;

    @Schema(description = "Last page of the sheet.")
    public Integer pagemax;

    @Schema(description = "Identifier of the related scan.")
    public Long scanId;
    @Schema(description = "Name of the related scan.")
    public String scanName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExamSheetDTO)) {
            return false;
        }

        return id != null && id.equals(((ExamSheetDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ExamSheetDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", pagemin=" + pagemin +
            ", pagemax=" + pagemax +
            ", scanId=" + scanId +
            ", scanName='" + scanName + "'" +
            "}";
    }
}
