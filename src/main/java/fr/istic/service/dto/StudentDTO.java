package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the {@link fr.istic.domain.Student} entity.
 */
@RegisterForReflection
public class StudentDTO implements Serializable {
    
    public Long id;

    @NotNull
    public String name;

    public String firstname;

    @NotNull
    public String ine;

    public String caslogin;

    public String mail;

    public Set<ExamSheetDTO> examSheets = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StudentDTO)) {
            return false;
        }

        return id != null && id.equals(((StudentDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "StudentDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", firstname='" + firstname + "'" +
            ", ine='" + ine + "'" +
            ", caslogin='" + caslogin + "'" +
            ", mail='" + mail + "'" +
            ", examSheets='" + examSheets + "'" +
            "}";
    }
}
