package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DTO for the {@link fr.istic.domain.Template} entity.
 */
@RegisterForReflection
public class TemplateDTO implements Serializable {

    public Long id;

    @NotNull
    public String name;

    public String contentContentType;

    @JsonProperty("mark")
    public Boolean mark;

    @JsonProperty("caseboxname")
    public Boolean caseboxname;


    public Boolean autoMapStudentCopyToList;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemplateDTO)) {
            return false;
        }

        return id != null && id.equals(((TemplateDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "TemplateDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", mark='" + mark + "'" +
            ", caseboxname='" + caseboxname + "'" +
            ", autoMapStudentCopyToList='" + autoMapStudentCopyToList + "'" +
            "}";
    }
}
