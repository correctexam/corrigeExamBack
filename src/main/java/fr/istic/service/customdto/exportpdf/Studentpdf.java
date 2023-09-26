package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Studentpdf {
    private String firstname;
    private long id;
    private String ine;
    private String mail;
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
