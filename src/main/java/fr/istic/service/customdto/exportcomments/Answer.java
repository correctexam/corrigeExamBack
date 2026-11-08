package fr.istic.service.customdto.exportcomments;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection

@Schema(description = "A student answer with its comments and prediction, used when exporting comments.")

public class Answer {
    @Schema(description = "Identifier of the exam sheet.")
    private Long sheetId;
    @Schema(description = "Name of the exam sheet.")
    private String sheetName;
    @Schema(description = "Grade of the answer.")
    private Double grade;
    @Schema(description = "First page of the sheet.")
    private Integer pagemin;
    @Schema(description = "Last page of the sheet.")
    private Integer pagemax;
    @Schema(description = "Comments on the answer.")
    private List<Comment> comments  = new ArrayList<>();
    @Schema(description = "Prediction associated with the answer.")
    private Prediction prediction;

    public Long getSheetId() { return sheetId; }
    public void setSheetId(Long value) { this.sheetId = value; }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String value) { this.sheetName = value; }

    public Double getGrade() { return grade; }
    public void setGrade(Double value) { this.grade = value; }

    public Integer getPagemin() { return pagemin; }
    public void setPagemin(Integer value) { this.pagemin = value; }

    public Integer getPagemax() { return pagemax; }
    public void setPagemax(Integer value) { this.pagemax = value; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> value) { this.comments = value; }

    public Prediction getPrediction() { return prediction; }
    public void setPrediction(Prediction value) { this.prediction = value; }
}
