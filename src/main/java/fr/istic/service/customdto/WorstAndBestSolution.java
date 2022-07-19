package fr.istic.service.customdto;

import java.util.ArrayList;
import java.util.List;

public class WorstAndBestSolution {
    private long numero;

    private List<String> bestSolutions = new ArrayList<>();
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
}
