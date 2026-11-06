package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A student with identity and group information, used to build or import students.")
public class StudentDTO {
    @Schema(description = "National student identifier (INE).")
    private String ine;
    @Schema(description = "Last name of the student.")
    private String nom;
    @Schema(description = "First name of the student.")
    private String prenom;
    @Schema(description = "Email address of the student.")
    private String mail;
    @Schema(description = "Group of the student.")
    private String groupe;

    public String getIne() { return ine; }
    public void setIne(String value) { this.ine = value; }

    public String getNom() { return nom; }
    public void setNom(String value) { this.nom = value; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String value) { this.prenom = value; }

    public String getMail() { return mail; }
    public void setMail(String value) { this.mail = value; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String value) { this.groupe = value; }
    @Override
    public String toString() {
        return "StudentDTO [groupe=" + groupe + ", ine=" + ine + ", mail=" + mail + ", nom=" + nom + ", prenom="
                + prenom + "]";
    }
}
