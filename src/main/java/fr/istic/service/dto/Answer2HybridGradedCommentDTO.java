package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Answer2HybridGradedComment} entity.
 */
@RegisterForReflection
@Schema(description = "Links a hybrid graded comment to a student answer with the step value applied.")
public class Answer2HybridGradedCommentDTO implements Serializable {

    @Schema(description = "Unique identifier of the association.")
    public Long id;

    @Schema(description = "Step value applied for this hybrid comment.")
    public Integer stepValue;

    @Schema(description = "Identifier of the related hybrid graded comment.")
    public Long hybridcommentsId;
    @Schema(description = "Text of the related hybrid graded comment.")
    public String hybridcommentsText;
    @Schema(description = "Identifier of the related student response.")
    public Long studentResponseId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Answer2HybridGradedCommentDTO)) {
            return false;
        }

        return id != null && id.equals(((Answer2HybridGradedCommentDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return (
            "Answer2HybridGradedCommentDTO{" +
            "id=" +
            id +
            ", stepValue=" +
            stepValue +
            ", hybridcommentsId=" +
            hybridcommentsId +
            ", hybridcommentsText='" +
            hybridcommentsText +
            "'" +
            ", studentResponseId=" +
            studentResponseId +
            "}"
        );
    }
}
