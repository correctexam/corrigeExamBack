package fr.istic.service.customdto.exportcomments;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection

@Schema(description = "All answers for a question together with their predictions.")

public class AnswersWithPredictionDto {
    @Schema(description = "Maximum grade of the question.")
    private Double maxgrade;
    @Schema(description = "Identifier of the question.")
    private Long qid;
    @Schema(description = "Answers with their predictions.")
    private List<Answer> answer  = new ArrayList<>();

    public Double getMaxgrade() { return maxgrade; }
    public void setMaxgrade(Double value) { this.maxgrade = value; }

    public Long getQid() { return qid; }
    public void setQid(Long value) { this.qid = value; }

    public List<Answer> getAnswer() { return answer; }
    public void setAnswer(List<Answer> value) { this.answer = value; }
}
