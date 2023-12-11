package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.Answer2HybridGradedComment} entity.
 */
@RegisterForReflection
public class Answer2HybridGradedCommentDTO implements Serializable {

    public Long id;

    public Integer stepValue;

    public Long hybridcommentsId;
    public String hybridcommentsText;
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
