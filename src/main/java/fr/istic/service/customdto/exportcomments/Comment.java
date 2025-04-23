package fr.istic.service.customdto.exportcomments;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection

public class Comment {
    private String text;
    private String description;
    private Double noteComments;

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public Double getNoteComments() { return noteComments; }
    public void setNoteComments(Double value) { this.noteComments = value; }
}
