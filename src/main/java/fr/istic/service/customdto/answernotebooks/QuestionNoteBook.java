package fr.istic.service.customdto.answernotebooks;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class QuestionNoteBook {
    private Long numero;
    private Double note;
    private Double notemax;

    public Long getNumero() { return numero; }
    public void setNumero(Long value) { this.numero = value; }

    public Double getNote() { return note; }
    public void setNote(Double value) { this.note = value; }

    public Double getNotemax() { return notemax; }
    public void setNotemax(Double value) { this.notemax = value; }
}
