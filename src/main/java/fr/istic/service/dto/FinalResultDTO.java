package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.FinalResult} entity.
 */
@RegisterForReflection
public class FinalResultDTO implements Serializable {
    
    public Long id;

    public Integer note;

    public Long studentId;
    public String studentName;
    public Long examId;
    public String examName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinalResultDTO)) {
            return false;
        }

        return id != null && id.equals(((FinalResultDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "FinalResultDTO{" +
            "id=" + id +
            ", note=" + note +
            ", studentId=" + studentId +
            ", studentName='" + studentName + "'" +
            ", examId=" + examId +
            ", examName='" + examName + "'" +
            "}";
    }
}
