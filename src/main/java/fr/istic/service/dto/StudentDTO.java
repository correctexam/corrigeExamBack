package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.Student} entity.
 */
@RegisterForReflection
@Schema(description = "A student enrolled in the grading system.")
public class StudentDTO implements Serializable {

    @Schema(description = "Unique identifier.")
    public Long id;

    @NotNull
    @Schema(description = "Last name of the student.", required=true)
    public String name;

    @Schema(description = "First name of the student.")
    public String firstname;

    @NotNull
    @Schema(description = "National student identifier (INE).", required=true)
    public String ine;

    @Schema(description = "CAS login of the student.")
    public String caslogin;

    @Schema(description = "Email address of the student.")
    public String mail;

    @Schema(description = "Set of exam sheets associated with the student.")
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
