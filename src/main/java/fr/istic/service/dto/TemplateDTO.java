package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;

/**
 * A DTO for the {@link fr.istic.domain.Template} entity.
 */
@RegisterForReflection
public class TemplateDTO implements Serializable {
    
    public Long id;

    @NotNull
    public String name;

    @Lob
    public byte[] content;

    public String contentContentType;


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
            ", content='" + content + "'" +
            "}";
    }
}
