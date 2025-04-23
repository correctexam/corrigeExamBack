package fr.istic.service.customdto.exportcomments;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection

public class AnswersWithPredictionDto {
    private Double maxgrade;
    private Long qid;
    private List<Answer> answer  = new ArrayList<>();

    public Double getMaxgrade() { return maxgrade; }
    public void setMaxgrade(Double value) { this.maxgrade = value; }

    public Long getQid() { return qid; }
    public void setQid(Long value) { this.qid = value; }

    public List<Answer> getAnswer() { return answer; }
    public void setAnswer(List<Answer> value) { this.answer = value; }
}
