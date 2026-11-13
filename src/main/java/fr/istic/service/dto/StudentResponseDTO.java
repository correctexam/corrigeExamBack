package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.StudentResponse} entity.
 */
@RegisterForReflection
@Schema(description = "The graded answer given by a student for a question of an exam sheet.")
public class StudentResponseDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Note awarded for the answer.")
    public Double note;

    @Schema(description = "Whether the answer is marked as starred.")
    public Boolean star;

    @Schema(description = "Whether the answer is marked as a worst answer.")
    public Boolean worststar;

    @Schema(description = "Identifier of the related question.")
    public Long questionId;
    @Schema(description = "Number of the related question.")
    public String questionNumero;
    @Schema(description = "Identifier of the related exam sheet.")
    public Long sheetId;
    @Schema(description = "Name of the related exam sheet.")
    public String sheetName;
    @Schema(description = "First page of the related sheet.")
    public Integer sheetPageMin;
    @Schema(description = "Last page of the related sheet.")
    public Integer sheetPageMax;
    @Schema(description = "Info about the grader of the answer.")
    public String correctedByInfo;
    @Schema(description = "Email of the grader of the answer.")
    public String correctedByMail;
    @Schema(description = "Date of the last modification of the answer.")
    public Instant lastModifiedDate;

    @Schema(description = "True when a grader changed the answer by hand: the QCM autocorrection keeps it. "
            + "Omitted or null in an update: the current value is kept.")
    public Boolean manuallyGraded;

    @Schema(description = "Set of text comments on the answer.")
    public Set<TextCommentDTO> textcomments = new HashSet<>();
    @Schema(description = "Set of graded comments on the answer.")
    public Set<GradedCommentDTO> gradedcomments = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StudentResponseDTO)) {
            return false;
        }

        return id != null && id.equals(((StudentResponseDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "StudentResponseDTO{" +
            "id=" + id +
            ", note=" + note +
            ", star='" + star + "'" +
            ", worststar='" + worststar + "'" +
            ", questionId=" + questionId +
            ", questionNumero='" + questionNumero + "'" +
            ", sheetId=" + sheetId +
            ", sheetName='" + sheetName + "'" +
            ", sheetPageMin='" + sheetPageMin + "'" +
            ", sheetPageMax='" + sheetPageMax + "'" +
            ", textcomments='" + textcomments + "'" +
            ", gradedcomments='" + gradedcomments + "'" +
            "}";
    }
}
