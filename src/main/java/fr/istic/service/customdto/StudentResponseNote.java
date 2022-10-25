package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StudentResponseNote {
    private Double currentNote;

    public Double getCurrentNote() { return currentNote; }
    public void setCurrentNote(Double value) { this.currentNote = value; }
    @Override
    public String toString() {
        return "StudentResponseNote [currentNote=" + currentNote + "]";
    }


}
