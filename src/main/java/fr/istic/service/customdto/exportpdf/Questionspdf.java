package fr.istic.service.customdto.exportpdf;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A question printed in the exported PDF.")
public class Questionspdf {
    @Schema(description = "Grading type of the question.")
    private String gradeType;
    @Schema(description = "Identifier of the question.")
    private long id;
    @Schema(description = "Number of the question.")
    private long numero;
    @Schema(description = "Points awarded by the question.")
    private Double point;
    @Schema(description = "Step value of the question.")
    private long step;
    @Schema(description = "Algorithm name of the question type.")
    private String typeAlgoName;
    @Schema(description = "Identifier of the question type.")
    private long typeID;
    @Schema(description = "Zone of the question.")
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
