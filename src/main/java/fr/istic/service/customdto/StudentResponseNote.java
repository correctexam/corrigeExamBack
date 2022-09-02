package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StudentResponseNote {
    private Integer currentNote;

    public Integer getCurrentNote() { return currentNote; }
    public void setCurrentNote(Integer value) { this.currentNote = value; }
    @Override
    public String toString() {
        return "StudentResponseNote [currentNote=" + currentNote + "]";
    }


}
