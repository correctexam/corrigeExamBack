package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Comments} entity.
 */
@RegisterForReflection
@Schema(description = "Stores the JSON payload of a comment associated with a zone of a student response.")
public class CommentsDTO implements Serializable {
    
    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Generated identifier of the zone the comment belongs to.")
    public String zonegeneratedid;

    @Schema(description = "JSON payload of the comment.")
    public String jsonData;

    @Schema(description = "Identifier of the related student response.")
    public Long studentResponseId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommentsDTO)) {
            return false;
        }

        return id != null && id.equals(((CommentsDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CommentsDTO{" +
            "id=" + id +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            ", jsonData='" + jsonData + "'" +
            ", studentResponseId=" + studentResponseId +
            "}";
    }
}
