package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.QuestionType} entity.
 */
@RegisterForReflection
public class QuestionTypeDTO implements Serializable {
    
    public Long id;

    @NotNull
    public String algoName;

    public String endpoint;

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
