package fr.istic.service.customdto.exportcomments;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Prediction {
    private String text;
    private Double predictionconfidence;

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }

    public Double getPredictionconfidence() { return predictionconfidence; }
    public void setPredictionconfidence(Double value) { this.predictionconfidence = value; }
}
