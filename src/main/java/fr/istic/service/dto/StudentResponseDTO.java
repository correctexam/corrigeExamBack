package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.StudentResponse} entity.
 */
@RegisterForReflection
public class StudentResponseDTO implements Serializable {
    
    public Long id;

    public Integer note;

    public Long questionId;
    public String questionNumero;
    public Long studentId;
    public String studentName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StudentResponseDTO)) {
            return false;
        }

        return id != null && id.equals(((StudentResponseDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "StudentResponseDTO{" +
            "id=" + id +
            ", note=" + note +
            ", questionId=" + questionId +
            ", questionNumero='" + questionNumero + "'" +
            ", studentId=" + studentId +
            ", studentName='" + studentName + "'" +
            "}";
    }
}
