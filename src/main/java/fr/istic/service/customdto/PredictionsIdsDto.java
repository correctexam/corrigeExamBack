package fr.istic.service.customdto;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Identifiers of predictions made for a given exam question.")
public class PredictionsIdsDto {
    @Schema(description = "Identifiers of the predictions.")
    private List<Long> predictionsids;
    @Schema(description = "Identifier of the related exam.")
    private long examId;
    @Schema(description = "Number of the related question.")
    private int numero;

    public long getExamId() {
        return this.examId;
    }

    public void setExamId(long examId) {
        this.examId = examId;
    }

    public int getNumero() {
        return this.numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public List<Long> getPredictionsids() { return predictionsids; }
    public void setPredictionsids(List<Long> value) { this.predictionsids = value; }
}
