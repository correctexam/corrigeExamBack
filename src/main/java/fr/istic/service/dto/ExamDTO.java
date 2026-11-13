package fr.istic.service.dto;


import fr.istic.domain.enumeration.StudentAccess;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Exam} entity.
 */
@RegisterForReflection
@Schema(description = "An exam built from a template, with its zones, scan file and course.")
public class ExamDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the exam.", required=true)
    public String name;
    @Column(name = "nbgrader")
    @Schema(description = "Whether the exam is graded by the built-in grader.", defaultValue="true")
    public Boolean nbgrader=true;

    @Schema(description = "What the students see from the student portal: HIDDEN, SHEET (scan only) or GRADED "
            + "(annotations and grade). Omitted or null in an update: unchanged (see PUT /api/exams/{id}/student-access).")
    public StudentAccess studentAccess;


    @Schema(description = "Identifier of the related template.")
    public Long templateId;
    @Schema(description = "Name of the related template.")
    public String templateName;
    @Schema(description = "Whether the template places the mark zone.")
    public Boolean templateMark;
    @Schema(description = "Whether the template places the name box.")
    public Boolean templateNameBoxCase;
    @Schema(description = "Identifier of the zone used for the student id.")
    public Long idzoneId;
    @Schema(description = "Identifier of the zone used for the student name.")
    public Long namezoneId;
    @Schema(description = "Identifier of the zone used for the student first name.")
    public Long firstnamezoneId;
    @Schema(description = "Identifier of the zone used for the exam note.")
    public Long notezoneId;
    @Schema(description = "Identifier of the related scan file.")
    public Long scanfileId;
    @Schema(description = "Name of the related scan file.")
    public String scanfileName;
    @Schema(description = "Identifier of the related course.")
    public Long courseId;
    @Schema(description = "Name of the related course.")
    public String courseName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExamDTO)) {
            return false;
        }

        return id != null && id.equals(((ExamDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ExamDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", nbgrader='" + nbgrader + "'" +
            ", templateId=" + templateId +
            ", templateName='" + templateName + "'" +
            ", templateMark='" + templateMark + "'" +
            ", templateNameBoxCase='" + templateNameBoxCase + "'" +
            ", idzoneId=" + idzoneId +
            ", namezoneId=" + namezoneId +
            ", firstnamezoneId=" + firstnamezoneId +
            ", notezoneId=" + notezoneId +
            ", scanfileId=" + scanfileId +
            ", scanfileName='" + scanfileName + "'" +
            ", courseId=" + courseId +
            ", courseName='" + courseName + "'" +
            "}";
    }
}
