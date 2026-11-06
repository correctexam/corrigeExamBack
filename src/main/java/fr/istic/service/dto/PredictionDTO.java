package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import jakarta.persistence.Lob;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Prediction} entity.
 */
@RegisterForReflection
@Schema(description = "An automatic prediction of the text for a question on an exam sheet.")
public class PredictionDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Predicted text value.")
    public String text;

    @Lob
    @Schema(description = "JSON payload of the prediction data.")
    public String jsonData;

    @Schema(description = "Number of the question the prediction targets.")
    public String questionNumber;

    @Schema(description = "Identifier of the related question.")
    public Long questionId;

    @Schema(description = "Identifier of the related exam sheet.")
    public Long sheetId;
    @Schema(description = "First page of the related sheet.")
    public Long sheetPageMin;
    @Schema(description = "Last page of the related sheet.")
    public Long sheetPageMax;

    @Schema(description = "Confidence score of the prediction.")
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
