package fr.istic.service.customdto;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Best and worst solutions for a given question.")
public class WorstAndBestSolution {
    @Schema(description = "Number of the question.")
    private long numero;

    @Schema(description = "Best solutions for the question.")
    private List<String> bestSolutions = new ArrayList<>();
    @Schema(description = "Worst solutions for the question.")
    private List<String> worstSolutions= new ArrayList<>();

    public WorstAndBestSolution(){

    }

    public WorstAndBestSolution(long numero) {
        this.numero = numero;
    }
    public long getNumero() { return numero; }
    public void setNumero(long value) { this.numero = value; }

    public List<String> getBestSolutions() { return bestSolutions; }
    public void setBestSolutions(List<String> value) { this.bestSolutions = value; }

    public List<String> getWorstSolutions() { return worstSolutions; }
    public void setWorstSolutions(List<String> value) { this.worstSolutions = value; }

    @Override
    public String toString() {
        return "WorstAndBestSolution [bestSolutions=" + bestSolutions + ", numero=" + numero + ", worstSolutions="
                + worstSolutions + "]";
    }


}
