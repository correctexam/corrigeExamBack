package fr.istic.service.customdto.exportcomments;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "An automatic prediction of an answer text.")
public class Prediction {
    @Schema(description = "Predicted text.")
    private String text;
    @Schema(description = "Confidence score of the prediction.")
    private Double predictionconfidence;

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }

    public Double getPredictionconfidence() { return predictionconfidence; }
    public void setPredictionconfidence(Double value) { this.predictionconfidence = value; }
}
