package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Lob;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.GradedComment} entity.
 */
@RegisterForReflection
@Schema(description = "A pre-defined graded comment reusable when marking a student answer.")
public class GradedCommentDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Generated identifier of the zone the comment is associated with.")
    public String zonegeneratedid;

    @Schema(description = "Text of the comment.")
    public String text;

    @Lob
    @Schema(description = "Long description of the comment.")
    public String description;

    @Schema(description = "Points awarded by this comment.")
    public Double grade;

    @Schema(description = "Identifier of the related question.")
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
