package fr.istic.service.customdto.exportpdf;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ExportPDFDto {
    private Zonepdf firstnamezonepdf;
    private long id;
    private String name;
    private Zonepdf namezonepdf;
    private long scanfileID;
    private List<Sheetspdf> sheetspdf;
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
