package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.ExamSheet} entity.
 */
@RegisterForReflection
public class ExamSheetDTO implements Serializable {

    public Long id;

    @NotNull
    public String name;

    public Integer pagemin;

    public Integer pagemax;

    public Long scanId;
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
