package fr.istic.service.customdto.answernotebooks;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "The note obtained by a student for a question in the answer book.")
public class QuestionNoteBook {
    @Schema(description = "Number of the question.")
    private Integer numero;
    @Schema(description = "Note obtained by the student.")
    private Double note;
    @Schema(description = "Maximum note of the question.")
    private Double notemax;

    public Integer getNumero() { return numero; }
    public void setNumero(Integer value) { this.numero = value; }

    public Double getNote() { return note; }
    public void setNote(Double value) { this.note = value; }

    public Double getNotemax() { return notemax; }
    public void setNotemax(Double value) { this.notemax = value; }
}
