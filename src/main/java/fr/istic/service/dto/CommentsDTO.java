package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.Comments} entity.
 */
@RegisterForReflection
public class CommentsDTO implements Serializable {
    
    public Long id;

    public String zonegeneratedid;

    public String jsonData;

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
