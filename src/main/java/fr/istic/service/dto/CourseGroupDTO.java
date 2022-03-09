package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.CourseGroup} entity.
 */
@RegisterForReflection
public class CourseGroupDTO implements Serializable {
    
    public Long id;

    @NotNull
    public String groupName;

    public Set<StudentDTO> students = new HashSet<>();
    public Long courseId;
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
