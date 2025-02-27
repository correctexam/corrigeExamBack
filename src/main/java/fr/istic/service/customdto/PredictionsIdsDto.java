package fr.istic.service.customdto;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
@RegisterForReflection
public class PredictionsIdsDto {
    private List<Long> predictionsids;
    private long examId;
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
