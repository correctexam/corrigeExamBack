package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Lob;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.HybridGradedComment} entity.
 */
@RegisterForReflection
@Schema(description = "A graded comment that can be applied relative or absolute, with an optional step value.")
public class HybridGradedCommentDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Text of the comment.")
    public String text;

    @Lob
    @Schema(description = "Long description of the comment.")
    public String description;

    @Schema(description = "Points awarded by this comment.")
    public Double grade;

    @Schema(description = "Whether the grade is applied relative to the step.")
    public Boolean relative;

    @Schema(description = "Step value used for relative grading.")
    public Integer step;

    @Schema(description = "Identifier of the related question.")
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
