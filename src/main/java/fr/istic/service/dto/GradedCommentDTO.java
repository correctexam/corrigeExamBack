package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Lob;

/**
 * A DTO for the {@link fr.istic.domain.GradedComment} entity.
 */
@RegisterForReflection
public class GradedCommentDTO implements Serializable {

    public Long id;

    public String zonegeneratedid;

    public String text;

    @Lob
    public String description;

    public Double grade;

    public Long questionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GradedCommentDTO)) {
            return false;
        }

        return id != null && id.equals(((GradedCommentDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "GradedCommentDTO{" +
            "id=" + id +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            ", text='" + text + "'" +
            ", description='" + description + "'" +
            ", grade=" + grade +
            ", questionId=" + questionId +
            "}";
    }
}
