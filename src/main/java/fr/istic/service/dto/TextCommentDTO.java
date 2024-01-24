package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Lob;

/**
 * A DTO for the {@link fr.istic.domain.TextComment} entity.
 */
@RegisterForReflection
public class TextCommentDTO implements Serializable {

    public Long id;

    public String text;

    @Lob
    public String description;

    public String zonegeneratedid;

    public Long questionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TextCommentDTO)) {
            return false;
        }

        return id != null && id.equals(((TextCommentDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "TextCommentDTO{" +
            "id=" + id +
            ", text='" + text + "'" +
            ", description='" + description + "'" +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            ", questionId=" + questionId +
            "}";
    }
}
