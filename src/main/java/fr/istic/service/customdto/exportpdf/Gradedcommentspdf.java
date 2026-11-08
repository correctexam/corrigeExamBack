package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A graded comment printed in the exported PDF.")
public class Gradedcommentspdf {
    @Schema(description = "Long description of the comment.")
    private String description;
    @Schema(description = "Points awarded by the comment.")
    private double grade;
    @Schema(description = "Text of the comment.")
    private String text;
    @Schema(description = "Generated identifier of the zone.")
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
