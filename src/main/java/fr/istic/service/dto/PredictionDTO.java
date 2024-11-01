package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

import fr.istic.domain.Prediction;
import jakarta.persistence.Lob;

/**
 * A DTO for the {@link fr.istic.domain.Prediction} entity.
 */
@RegisterForReflection
public class PredictionDTO implements Serializable {

    public Long id;

    public String text;

    @Lob
    public String jsonData;

    public String zonegeneratedid;

    public String questionNumber;

    public Long questionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PredictionDTO)) {
            return false;
        }

        return id != null && id.equals(((PredictionDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "PredictionDTO{" +
            "id=" + id +
            ", text='" + text + "'" +
            ", jsonData='" + jsonData + "'" +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            ", questionNumber='" + questionNumber + "'" +
            ", questionId=" + questionId +
            "}";
    }
}
