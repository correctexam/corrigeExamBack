package fr.istic.service.customdto.exportcomments;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
@RegisterForReflection

public class Answer {
    private Long sheetId;
    private String sheetName;
    private Double grade;
    private Integer pagemin;
    private Integer pagemax;
    private List<Comment> comments  = new ArrayList<>();
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
