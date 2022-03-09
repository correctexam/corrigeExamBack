package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.Course} entity.
 */
@RegisterForReflection
public class CourseDTO implements Serializable {
    
    public Long id;

    @NotNull
    public String name;

    public Long profId;
    public String profLogin;

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
            ", profId=" + profId +
            ", profLogin='" + profLogin + "'" +
            "}";
    }
}
