package fr.istic.service.customdto.exportpdf;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StudentResponsepdf {
    private List<Gradedcommentspdf> gradedcommentspdf;
    private long id;
    private long note;
    private long questionID;
    private String questionNumero;
    private long sheetID;
    private boolean star;
    private List<Textcommentspdf> textcommentspdf;
    private boolean worststar;

    public List<Gradedcommentspdf> getGradedcommentspdf() { return gradedcommentspdf; }
    public void setGradedcommentspdf(List<Gradedcommentspdf> value) { this.gradedcommentspdf = value; }

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }

    public long getNote() { return note; }
    public void setNote(long value) { this.note = value; }

    public long getQuestionID() { return questionID; }
    public void setQuestionID(long value) { this.questionID = value; }

    public String getQuestionNumero() { return questionNumero; }
    public void setQuestionNumero(String value) { this.questionNumero = value; }

    public long getSheetID() { return sheetID; }
    public void setSheetID(long value) { this.sheetID = value; }

    public boolean getStar() { return star; }
    public void setStar(boolean value) { this.star = value; }

    public List<Textcommentspdf> getTextcommentspdf() { return textcommentspdf; }
    public void setTextcommentspdf(List<Textcommentspdf> value) { this.textcommentspdf = value; }

    public boolean getWorststar() { return worststar; }
    public void setWorststar(boolean value) { this.worststar = value; }
}
