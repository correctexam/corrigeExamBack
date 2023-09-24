package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Questionspdf {
    private String gradeType;
    private long id;
    private long numero;
    private Double point;
    private long step;
    private String typeAlgoName;
    private long typeID;
    private Zonepdf zonepdf;

    public String getGradeType() { return gradeType; }
    public void setGradeType(String value) { this.gradeType = value; }

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }

    public long getNumero() { return numero; }
    public void setNumero(long value) { this.numero = value; }

    public Double getPoint() { return point; }
    public void setPoint(Double value) { this.point = value; }

    public long getStep() { return step; }
    public void setStep(long value) { this.step = value; }

    public String getTypeAlgoName() { return typeAlgoName; }
    public void setTypeAlgoName(String value) { this.typeAlgoName = value; }

    public long getTypeID() { return typeID; }
    public void setTypeID(long value) { this.typeID = value; }

    public Zonepdf getZonepdf() { return zonepdf; }
    public void setZonepdf(Zonepdf value) { this.zonepdf = value; }
}
