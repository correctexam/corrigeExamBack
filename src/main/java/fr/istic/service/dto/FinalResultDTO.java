package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * A DTO for the {@link fr.istic.domain.FinalResult} entity.
 */
@RegisterForReflection
@Schema(description = "The final computed result of a student for an exam.")
public class FinalResultDTO implements Serializable {
    
    @Schema(description = "Unique identifier.")
    public Long id;

    @Schema(description = "Final note awarded.")
    public Integer note;

    @Schema(description = "Identifier of the related student.")
    public Long studentId;
    @Schema(description = "Name of the related student.")
    public String studentName;
    @Schema(description = "Identifier of the related exam.")
    public Long examId;
    @Schema(description = "Name of the related exam.")
    public String examName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinalResultDTO)) {
            return false;
        }

        return id != null && id.equals(((FinalResultDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "FinalResultDTO{" +
            "id=" + id +
            ", note=" + note +
            ", studentId=" + studentId +
            ", studentName='" + studentName + "'" +
            ", examId=" + examId +
            ", examName='" + examName + "'" +
            "}";
    }
}
