package fr.istic.service.customdto.correctexamstate;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Marking state of a question.")
public class QuestionStateDTO {
    @Schema(description = "Identifier of the question.")
    private long id;
    @Schema(description = "Number of the question.")
    private long numero;
    @Schema(description = "Label of the question.")
    private String libelle;


    @Schema(description = "Number of answered sheets.")
    private long answeredSheets;
    @Schema(description = "Index of the first unmarked sheet.")
    private long firstUnmarkedSheet;
    @Schema(description = "Indices of the unmarked sheets.")
    private List<Integer> unmarkedSheetIndex;
    @Schema(description = "Whether horizontal answers are corrected in random order.")
    private boolean randomHorizontalCorrection;






    public long getId() {
        return id;
    }

    public void setId(long value) {
        this.id = value;
    }

    public long getNumero() {
        return numero;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setNumero(long value) {
        this.numero = value;
    }

    public long getAnsweredSheets() {
        return answeredSheets;
    }

    public void setAnsweredSheets(long value) {
        this.answeredSheets = value;
    }

    public long getFirstUnmarkedSheet() {
        return firstUnmarkedSheet;
    }

    public void setFirstUnmarkedSheet(long value) {
        this.firstUnmarkedSheet = value;
    }


    public List<Integer> getUnmarkedSheetIndex() {
        return unmarkedSheetIndex;
    }

    public void setUnmarkedSheetIndex(List<Integer> unmarkedSheetIndex) {
        this.unmarkedSheetIndex = unmarkedSheetIndex;
    }

    public boolean isRandomHorizontalCorrection() {
        return randomHorizontalCorrection;
    }

    public void setRandomHorizontalCorrection(boolean randomHorizontalCorrection) {
        this.randomHorizontalCorrection = randomHorizontalCorrection;
    }
}
