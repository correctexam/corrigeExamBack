package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Template} entity.
 */
@RegisterForReflection
@Schema(description = "An exam template describing the layout of the sheets.")
public class TemplateDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the template.", required=true)
    public String name;

    @Schema(description = "MIME content type of the template image.")
    public String contentContentType;

    @JsonProperty("mark")
    @Schema(description = "Whether the template contains a mark zone.", defaultValue="false")
    public Boolean mark;

    @JsonProperty("caseboxname")
    @Schema(description = "Whether the template contains case boxes for names.", defaultValue="false")
    public Boolean caseboxname;


    @Schema(description = "Whether student copies are automatically mapped to the student list.")
    public Boolean autoMapStudentCopyToList;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemplateDTO)) {
            return false;
        }

        return id != null && id.equals(((TemplateDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "TemplateDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", mark='" + mark + "'" +
            ", caseboxname='" + caseboxname + "'" +
            ", autoMapStudentCopyToList='" + autoMapStudentCopyToList + "'" +
            "}";
    }
}
