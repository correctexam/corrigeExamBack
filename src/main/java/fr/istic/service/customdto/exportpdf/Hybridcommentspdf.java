package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Hybridcommentspdf {
    private String description;
    private double grade;
    private String text;
    private double stepMax;
    private double stepValue;
    private boolean relative;
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
