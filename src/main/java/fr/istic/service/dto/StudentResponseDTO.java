package fr.istic.service.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the {@link fr.istic.domain.StudentResponse} entity.
 */
@RegisterForReflection
public class StudentResponseDTO implements Serializable {

    public Long id;

    public Double note;

    public Boolean star;

    public Boolean worststar;

    public Long questionId;
    public String questionNumero;
    public Long sheetId;
    public String sheetName;
    public Set<TextCommentDTO> textcomments = new HashSet<>();
    public Set<GradedCommentDTO> gradedcomments = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StudentResponseDTO)) {
            return false;
        }

        return id != null && id.equals(((StudentResponseDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "StudentResponseDTO{" +
            "id=" + id +
            ", note=" + note +
            ", star='" + star + "'" +
            ", worststar='" + worststar + "'" +
            ", questionId=" + questionId +
            ", questionNumero='" + questionNumero + "'" +
            ", sheetId=" + sheetId +
            ", sheetName='" + sheetName + "'" +
            ", textcomments='" + textcomments + "'" +
            ", gradedcomments='" + gradedcomments + "'" +
            "}";
    }
}
