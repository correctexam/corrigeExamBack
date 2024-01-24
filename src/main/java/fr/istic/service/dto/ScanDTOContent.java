package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Lob;

/**
 * A DTO for the {@link fr.istic.domain.Scan} entity.
 */
@RegisterForReflection
public class ScanDTOContent implements Serializable {

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
        if (!(o instanceof ScanDTO)) {
            return false;
        }

        return id != null && id.equals(((ScanDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ScanDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", content='" + content + "'" +
            "}";
    }
}
