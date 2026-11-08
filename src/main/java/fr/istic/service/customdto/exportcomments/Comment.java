package fr.istic.service.customdto.exportcomments;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection

@Schema(description = "A comment on an answer, used when exporting comments.")

public class Comment {
    @Schema(description = "Text of the comment.")
    private String text;
    @Schema(description = "Long description of the comment.")
    private String description;
    @Schema(description = "Note of the comment.")
    private Double noteComments;

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public Double getNoteComments() { return noteComments; }
    public void setNoteComments(Double value) { this.noteComments = value; }
}
