package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;

/**
 * A DTO for the {@link fr.istic.domain.HybridGradedComment} entity.
 */
@RegisterForReflection
public class HybridGradedCommentDTO implements Serializable {

    public Long id;

    public String text;

    @Lob
    public String description;

    public Double grade;

    public Boolean relative;

    public Integer step;

    public Long questionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HybridGradedCommentDTO)) {
            return false;
        }

        return id != null && id.equals(((HybridGradedCommentDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return (
            "HybridGradedCommentDTO{" +
            "id=" +
            id +
            ", text='" +
            text +
            "'" +
            ", description='" +
            description +
            "'" +
            ", grade=" +
            grade +
            ", relative='" +
            relative +
            "'" +
            ", step=" +
            step +
            ", questionId=" +
            questionId +
            "}"
        );
    }
}
