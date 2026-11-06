package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;

import fr.istic.domain.enumeration.GradeType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Question} entity.
 */
@RegisterForReflection
@Schema(description = "A question of an exam with its grading configuration and answer zone.")
public class QuestionDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Number of the question.", required=true)
    public Integer numero;

    @Schema(description = "Number of points awarded for this question.")
    public Double point;

    @Schema(description = "Step value used for this question.")
    public Integer step;

    @Schema(description = "Expression defining a valid answer.")
    public String validExpression;

    @Schema(description = "Label of the question.")
    public String libelle;

    @Schema(description = "Grading type applied to this question.")
    public GradeType gradeType;

    @Schema(description = "Default number of points.")
    public Integer defaultpoint;

    @Schema(description = "Identifier of the related zone.")
    public Long zoneId;
    @Schema(description = "DTO of the related zone.")
    public ZoneDTO zoneDTO;
    @Schema(description = "Identifier of the related question type.")
    public Long typeId;
    @Schema(description = "Algorithm name of the related question type.")
    public String typeAlgoName;
    @Schema(description = "Identifier of the related exam.")
    public Long examId;
    @Schema(description = "Name of the related exam.")
    public String examName;
    @Schema(description = "Whether horizontal answers are corrected in random order.")
    public Boolean randomHorizontalCorrection;
    @Schema(description = "Whether the grade can exceed the maximum.")
    public Boolean canExceedTheMax;
    @Schema(description = "Whether the grade can be negative.")
    public Boolean canBeNegative;
    @Schema(description = "Whether the question is ignored in the global grade scale.")
    public Boolean mustBeIgnoreInGlobalScale;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuestionDTO)) {
            return false;
        }

        return id != null && id.equals(((QuestionDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "QuestionDTO{" +
            "id=" + id +
            ", numero=" + numero +
            ", point=" + point +
            ", step=" + step +
            ", validExpression='" + validExpression + "'" +
            ", gradeType='" + gradeType + "'" +
            ", zoneId=" + zoneId +
            ", typeId=" + typeId +
            ", typeAlgoName='" + typeAlgoName + "'" +
            ", examId=" + examId +
            ", examName='" + examName + "'" +
            "}";
    }
}
