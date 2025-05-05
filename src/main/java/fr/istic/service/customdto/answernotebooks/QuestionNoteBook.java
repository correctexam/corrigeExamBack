package fr.istic.service.customdto.answernotebooks;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class QuestionNoteBook {
    private Integer numero;
    private Double note;
    private Double notemax;

    public Integer getNumero() { return numero; }
    public void setNumero(Integer value) { this.numero = value; }

    public Double getNote() { return note; }
    public void setNote(Double value) { this.note = value; }

    public Double getNotemax() { return notemax; }
    public void setNotemax(Double value) { this.notemax = value; }
}
