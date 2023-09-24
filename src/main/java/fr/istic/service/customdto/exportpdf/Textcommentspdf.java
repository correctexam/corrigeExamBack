package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Textcommentspdf {
    private String description;
    private String text;

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public String getText() { return text; }
    public void setText(String value) { this.text = value; }
}
