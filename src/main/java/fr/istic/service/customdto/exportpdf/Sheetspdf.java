package fr.istic.service.customdto.exportpdf;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection

@Schema(description = "An exam sheet printed in the exported PDF.")

public class Sheetspdf {
    @Schema(description = "Identifier of the sheet.")
    private long id;
    @Schema(description = "Name of the sheet.")
    private String name;
    @Schema(description = "Final result of the sheet.")
    private double finalresult;
    @Schema(description = "Last page of the sheet.")
    private long pagemax;
    @Schema(description = "First page of the sheet.")
    private long pagemin;
    @Schema(description = "Students of the sheet.")
    private List<Studentpdf> studentpdf;
    @Schema(description = "Student responses of the sheet.")
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
