package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Course} entity.
 */
@RegisterForReflection
@Schema(description = "A course grouping exams and assigned to professors.")
public class CourseDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Name of the course.", required=true)
    public String name;

    @Schema(description = "Whether the course is archived.", defaultValue="false")
    public Boolean archived = false;


    @Schema(description = "Set of professors (users) in charge of the course.")
    public Set<UserDTO> profs = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseDTO)) {
            return false;
        }

        return id != null && id.equals(((CourseDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CourseDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", archived='" + archived + "'" +
            ", profs='" + profs + "'" +
            "}";
    }
}
