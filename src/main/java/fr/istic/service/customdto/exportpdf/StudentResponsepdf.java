package fr.istic.service.customdto.exportpdf;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A student answer printed in the exported PDF.")
public class StudentResponsepdf {
    @Schema(description = "Graded comments on the answer.")
    private List<Gradedcommentspdf> gradedcommentspdf;
    @Schema(description = "Hybrid graded comments on the answer.")
    private List<Hybridcommentspdf> hybridcommentspdf;


    @Schema(description = "Identifier of the answer.")
    private long id;
    @Schema(description = "Note of the answer.")
    private long note;
    @Schema(description = "Identifier of the question.")
    private long questionID;
    @Schema(description = "Number of the question.")
    private String questionNumero;
    @Schema(description = "Identifier of the sheet.")
    private long sheetID;
    @Schema(description = "Whether the answer is starred.")
    private boolean star;
    @Schema(description = "Text comments on the answer.")
    private List<Textcommentspdf> textcommentspdf;
    @Schema(description = "Whether the answer is marked as a worst answer.")
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

    public List<Hybridcommentspdf> getHybridcommentspdf() {
        return this.hybridcommentspdf;
    }

    public void setHybridcommentspdf(List<Hybridcommentspdf> hybridcommentspdf) {
        this.hybridcommentspdf = hybridcommentspdf;
    }


    public boolean isStar() {
        return this.star;
    }

    public boolean isWorststar() {
        return this.worststar;
    }
}
