package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.CourseGroup} entity.
 */
@RegisterForReflection
@Schema(description = "A group of students within a course.")
public class CourseGroupDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the group.", required=true)
    public String groupName;

    @Schema(description = "Set of students belonging to the group.")
    public Set<StudentDTO> students = new HashSet<>();
    @Schema(description = "Identifier of the related course.")
    public Long courseId;
    @Schema(description = "Name of the related course.")
    public String courseName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseGroupDTO)) {
            return false;
        }

        return id != null && id.equals(((CourseGroupDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CourseGroupDTO{" +
            "id=" + id +
            ", groupName='" + groupName + "'" +
            ", students='" + students + "'" +
            ", courseId=" + courseId +
            ", courseName='" + courseName + "'" +
            "}";
    }
}
