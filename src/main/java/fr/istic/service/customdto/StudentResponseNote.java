package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "The updated note of a student response.")
public class StudentResponseNote {
    @Schema(description = "Current note of the student response.")
    private Double currentNote;

    public Double getCurrentNote() { return currentNote; }
    public void setCurrentNote(Double value) { this.currentNote = value; }
    @Override
    public String toString() {
        return "StudentResponseNote [currentNote=" + currentNote + "]";
    }


}
