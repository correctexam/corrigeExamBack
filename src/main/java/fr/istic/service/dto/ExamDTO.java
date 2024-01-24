package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.Exam} entity.
 */
@RegisterForReflection
public class ExamDTO implements Serializable {

    public Long id;

    @NotNull
    public String name;

    public Long templateId;
    public String templateName;
    public Long idzoneId;
    public Long namezoneId;
    public Long firstnamezoneId;
    public Long notezoneId;
    public Long scanfileId;
    public String scanfileName;
    public Long courseId;
    public String courseName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExamDTO)) {
            return false;
        }

        return id != null && id.equals(((ExamDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ExamDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", templateId=" + templateId +
            ", templateName='" + templateName + "'" +
            ", idzoneId=" + idzoneId +
            ", namezoneId=" + namezoneId +
            ", firstnamezoneId=" + firstnamezoneId +
            ", notezoneId=" + notezoneId +
            ", scanfileId=" + scanfileId +
            ", scanfileName='" + scanfileName + "'" +
            ", courseId=" + courseId +
            ", courseName='" + courseName + "'" +
            "}";
    }
}
