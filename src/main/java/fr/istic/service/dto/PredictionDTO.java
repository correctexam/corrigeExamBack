package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
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

    public String questionNumber;

    public Long questionId;

    public Long sheetId;
    public Long sheetPageMin;
    public Long sheetPageMax;

    public Double predictionconfidence;


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
            ", predictionconfidence='" + predictionconfidence + "'" +
            ", questionNumber='" + questionNumber + "'" +
            ", questionId=" + questionId +
            ", sheetId=" + sheetId +
            "}";
    }
}
