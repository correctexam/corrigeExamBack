package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.QuestionType} entity.
 */
@RegisterForReflection
@Schema(description = "A type of question defining the grading algorithm and its processing endpoints.")
public class QuestionTypeDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the grading algorithm.", required=true)
    public String algoName;

    @Schema(description = "Endpoint called to grade this question type.")
    public String endpoint;

    @Schema(description = "JavaScript function used to process this question type.")
    public String jsFunction;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuestionTypeDTO)) {
            return false;
        }

        return id != null && id.equals(((QuestionTypeDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "QuestionTypeDTO{" +
            "id=" + id +
            ", algoName='" + algoName + "'" +
            ", endpoint='" + endpoint + "'" +
            ", jsFunction='" + jsFunction + "'" +
            "}";
    }
}
