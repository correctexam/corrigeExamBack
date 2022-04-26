package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

/**
 * A StudentResponse.
 */
@Entity
@Table(name = "student_response")
@Cacheable
@RegisterForReflection
public class StudentResponse extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "note")
    public Integer note;

    @OneToMany(mappedBy = "studentResponse")
    public Set<Comments> comments = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonbTransient
    public Question question;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    @JsonbTransient
    public ExamSheet sheet;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "student_response_textcomments",
               joinColumns = @JoinColumn(name = "student_response_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "textcomments_id", referencedColumnName = "id"))
    @JsonbTransient
    public Set<TextComment> textcomments = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "student_response_gradedcomments",
               joinColumns = @JoinColumn(name = "student_response_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "gradedcomments_id", referencedColumnName = "id"))
    @JsonbTransient
    public Set<GradedComment> gradedcomments = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StudentResponse)) {
            return false;
        }
        return id != null && id.equals(((StudentResponse) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "StudentResponse{" +
            "id=" + id +
            ", note=" + note +
            "}";
    }

    public StudentResponse update() {
        return update(this);
    }

    public StudentResponse persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static StudentResponse update(StudentResponse studentResponse) {
        if (studentResponse == null) {
            throw new IllegalArgumentException("studentResponse can't be null");
        }
        var entity = StudentResponse.<StudentResponse>findById(studentResponse.id);
        if (entity != null) {
            entity.note = studentResponse.note;
            entity.comments = studentResponse.comments;
            entity.question = studentResponse.question;
            entity.sheet = studentResponse.sheet;
            entity.textcomments = studentResponse.textcomments;
            entity.gradedcomments = studentResponse.gradedcomments;
        }
        return entity;
    }

    public static StudentResponse persistOrUpdate(StudentResponse studentResponse) {
        if (studentResponse == null) {
            throw new IllegalArgumentException("studentResponse can't be null");
        }
        if (studentResponse.id == null) {
            persist(studentResponse);
            return studentResponse;
        } else {
            return update(studentResponse);
        }
    }

    public static PanacheQuery<StudentResponse> findAllWithEagerRelationships() {
        return find("select distinct studentResponse from StudentResponse studentResponse left join fetch studentResponse.textcomments left join fetch studentResponse.gradedcomments");
    }

    public static Optional<StudentResponse> findOneWithEagerRelationships(Long id) {
        return find("select studentResponse from StudentResponse studentResponse left join fetch studentResponse.textcomments left join fetch studentResponse.gradedcomments where studentResponse.id =?1", id).firstResultOptional();
    }

    public static PanacheQuery<StudentResponse> findStudentResponsesbysheetIdAndquestionId( long sheetId, long questionId) {
        return find("select sr from StudentResponse sr where sr.sheet.id =?1 and question.id=?2", sheetId, questionId);
    }

}
