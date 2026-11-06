package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Lob;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Template} entity.
 */
@RegisterForReflection
@Schema(description = "An exam template including its binary image content.")
public class TemplateDTOContent implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the template.", required=true)
    public String name;

    @Lob
    @Schema(description = "Binary content of the template image.")
    public byte[] content;

    @Schema(description = "MIME content type of the template image.")
    public String contentContentType;

    @Schema(description = "Whether the template contains a mark zone.")
    public Boolean mark;

    @Schema(description = "Whether student copies are automatically mapped to the student list.")
    public Boolean autoMapStudentCopyToList;

    @JsonProperty("caseboxname")
    @Schema(description = "Whether the template contains case boxes for names.")
    public Boolean caseboxname;

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
            ", caseboxname='" + caseboxname + "'" +
            ", content='" + content + "'" +
            ", mark='" + mark + "'" +
            ", autoMapStudentCopyToList='" + autoMapStudentCopyToList + "'" +
            "}";
    }
}
