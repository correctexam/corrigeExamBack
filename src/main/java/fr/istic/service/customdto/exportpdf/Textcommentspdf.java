package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A text comment printed in the exported PDF.")
public class Textcommentspdf {
    @Schema(description = "Long description of the comment.")
    private String description;
    @Schema(description = "Text of the comment.")
    private String text;

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }
}
