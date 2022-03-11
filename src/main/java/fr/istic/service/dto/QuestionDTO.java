package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.Question} entity.
 */
@RegisterForReflection
public class QuestionDTO implements Serializable {
    
    public Long id;

    @NotNull
    public Integer numero;

    public Integer point;

    public Long zoneId;
    public Long typeId;
    public String typeAlgoName;
    public Long examId;
    public String examName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuestionDTO)) {
            return false;
        }

        return id != null && id.equals(((QuestionDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "QuestionDTO{" +
            "id=" + id +
            ", numero=" + numero +
            ", point=" + point +
            ", zoneId=" + zoneId +
            ", typeId=" + typeId +
            ", typeAlgoName='" + typeAlgoName + "'" +
            ", examId=" + examId +
            ", examName='" + examName + "'" +
            "}";
    }
}
