package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A student printed in the exported PDF.")
public class Studentpdf {
    @Schema(description = "First name of the student.")
    private String firstname;
    @Schema(description = "Identifier of the student.")
    private long id;
    @Schema(description = "National student identifier (INE).")
    private String ine;
    @Schema(description = "Email of the student.")
    private String mail;
    @Schema(description = "Last name of the student.")
    private String name;

    public String getFirstname() { return firstname; }
    public void setFirstname(String value) { this.firstname = value; }

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }

    public String getIne() { return ine; }
    public void setIne(String value) { this.ine = value; }

    public String getMail() { return mail; }
    public void setMail(String value) { this.mail = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
}
