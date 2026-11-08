package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Lob;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.TextComment} entity.
 */
@RegisterForReflection
@Schema(description = "A free-text comment attached to a student answer.")
public class TextCommentDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Text of the comment.")
    public String text;

    @Lob
    @Schema(description = "Long description of the comment.")
    public String description;

    @Schema(description = "Generated identifier of the zone the comment is associated with.")
    public String zonegeneratedid;

    @Schema(description = "Identifier of the related question.")
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
