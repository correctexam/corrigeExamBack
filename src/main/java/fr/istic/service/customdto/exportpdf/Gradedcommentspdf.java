package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Gradedcommentspdf {
    private String description;
    private double grade;
    private String text;
    private String zonegeneratedid;

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public double getGrade() { return grade; }
    public void setGrade(double value) { this.grade = value; }

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }

    public String getZonegeneratedid() { return zonegeneratedid; }
    public void setZonegeneratedid(String value) { this.zonegeneratedid = value; }
}
