package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A hybrid graded comment printed in the exported PDF.")
public class Hybridcommentspdf {
    @Schema(description = "Long description of the comment.")
    private String description;
    @Schema(description = "Points awarded by the comment.")
    private double grade;
    @Schema(description = "Text of the comment.")
    private String text;
    @Schema(description = "Maximum step value.")
    private double stepMax;
    @Schema(description = "Applied step value.")
    private double stepValue;
    @Schema(description = "Whether the grade is applied relative to the step.")
    private boolean relative;
    @Schema(description = "Effective grade of the answer.")
    private double answerGrade;




    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public double getGrade() { return grade; }
    public void setGrade(double value) { this.grade = value; }

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }


        public double getStepMax() {
        return this.stepMax;
    }

    public void setStepMax(double stepMax) {
        this.stepMax = stepMax;
    }

    public double getStepValue() {
        return this.stepValue;
    }

    public void setStepValue(double stepValue) {
        this.stepValue = stepValue;
    }

    public boolean isRelative() {
        return this.relative;
    }

    public boolean getRelative() {
        return this.relative;
    }

    public void setRelative(boolean relative) {
        this.relative = relative;
    }


    public double getAnswerGrade() {
        return this.answerGrade;
    }

    public void setAnswerGrade(double answerGrade) {
        this.answerGrade = answerGrade;
    }

}
