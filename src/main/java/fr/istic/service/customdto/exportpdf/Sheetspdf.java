package fr.istic.service.customdto.exportpdf;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection

public class Sheetspdf {
    private long id;
    private String name;
    private double finalresult;
    private long pagemax;
    private long pagemin;
    private List<Studentpdf> studentpdf;
    private List<StudentResponsepdf> studentResponsepdf;

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public double getFinalresult() { return finalresult; }
    public void setFinalresult(double value) { this.finalresult = value; }

    public long getPagemax() { return pagemax; }
    public void setPagemax(long value) { this.pagemax = value; }

    public long getPagemin() { return pagemin; }
    public void setPagemin(long value) { this.pagemin = value; }

    public List<Studentpdf> getStudentpdf() { return studentpdf; }
    public void setStudentpdf(List<Studentpdf> value) { this.studentpdf = value; }

    public List<StudentResponsepdf> getStudentResponsepdf() { return studentResponsepdf; }
    public void setStudentResponsepdf(List<StudentResponsepdf> value) { this.studentResponsepdf = value; }
}
