package fr.istic.service.customdto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DTO for the {@link fr.istic.domain.Template} entity.
 */
@RegisterForReflection
public class TemplateCaseDTO implements Serializable {

    public Long id;


    public String contentContentType;

    @JsonProperty("caseboxname")
    public Boolean caseboxname;



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemplateCaseDTO)) {
            return false;
        }

        return id != null && id.equals(((TemplateCaseDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "TemplateCaseDTO{" +
            "id=" + id +
            ", caseboxname='" + caseboxname + "'" +
            "}";
    }
}
