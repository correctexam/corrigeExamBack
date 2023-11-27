package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;

/**
 * A DTO for the {@link fr.istic.domain.Zone} entity.
 */
@RegisterForReflection
public class ZoneDTO implements Serializable {

    public Long id;

    public Integer pageNumber;

    public Integer xInit;

    public Integer yInit;

    public Integer width;

    public Integer height;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ZoneDTO)) {
            return false;
        }

        return id != null && id.equals(((ZoneDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ZoneDTO{" +
            "id=" + id +
            ", pageNumber=" + pageNumber +
            ", xInit=" + xInit +
            ", yInit=" + yInit +
            ", width=" + width +
            ", height=" + height +
            "}";
    }
}
