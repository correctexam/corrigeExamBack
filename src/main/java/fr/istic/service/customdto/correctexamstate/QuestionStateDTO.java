package fr.istic.service.customdto.correctexamstate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class QuestionStateDTO {
    private long id;
    private long numero;
    private String libelle;


    private long answeredSheets;
    private long firstUnmarkedSheet;

    public long getId() {
        return id;
    }

    public void setId(long value) {
        this.id = value;
    }

    public long getNumero() {
        return numero;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setNumero(long value) {
        this.numero = value;
    }

    public long getAnsweredSheets() {
        return answeredSheets;
    }

    public void setAnsweredSheets(long value) {
        this.answeredSheets = value;
    }

    public long getFirstUnmarkedSheet() {
        return firstUnmarkedSheet;
    }

    public void setFirstUnmarkedSheet(long value) {
        this.firstUnmarkedSheet = value;
    }
}
