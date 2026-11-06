package fr.istic.service.customdto.exportpdf;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Data required to build the exported PDF of an exam.")
public class ExportPDFDto {
    @Schema(description = "Zone of the student first name.")
    private Zonepdf firstnamezonepdf;
    @Schema(description = "Identifier of the exam.")
    private long id;

    @Schema(description = "Name of the exam.")
    private String name;
    @Schema(description = "Zone of the student name.")
    private Zonepdf namezonepdf;
    @Schema(description = "Identifier of the scan file.")
    private long scanfileID;
    @Schema(description = "Sheets of the exam.")
    private List<Sheetspdf> sheetspdf;
    @Schema(description = "Questions of the exam.")
    private List<Questionspdf> questionspdf;

    public Zonepdf getFirstnamezonepdf() { return firstnamezonepdf; }
    public void setFirstnamezonepdf(Zonepdf value) { this.firstnamezonepdf = value; }

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public Zonepdf getNamezonepdf() { return namezonepdf; }
    public void setNamezonepdf(Zonepdf value) { this.namezonepdf = value; }

    public long getScanfileID() { return scanfileID; }
    public void setScanfileID(long value) { this.scanfileID = value; }

    public List<Sheetspdf> getSheetspdf() { return sheetspdf; }
    public void setSheetspdf(List<Sheetspdf> value) { this.sheetspdf = value; }

    public List<Questionspdf> getQuestionspdf() { return questionspdf; }
    public void setQuestionspdf(List<Questionspdf> value) { this.questionspdf = value; }
}
