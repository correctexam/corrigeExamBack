package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StudentDTO {
    private String ine;
    private String nom;
    private String prenom;
    private String mail;
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
