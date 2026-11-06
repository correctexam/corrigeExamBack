package fr.istic.service.customdto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Template} entity.
 */
@RegisterForReflection
@Schema(description = "An exam template with its case-box configuration only.")
public class TemplateCaseDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;


    @Schema(description = "MIME content type of the template image.")
    public String contentContentType;

    @JsonProperty("caseboxname")
    @Schema(description = "Whether the template contains case boxes for names.")
    public Boolean caseboxname;



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemplateCaseDTO)) {
            return false;
        }

        return id != null && id.equals(((TemplateCaseDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "TemplateCaseDTO{" +
            "id=" + id +
            ", caseboxname='" + caseboxname + "'" +
            "}";
    }
}
