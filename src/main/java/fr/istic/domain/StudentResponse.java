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
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<Comments> comments = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonbTransient
    public Question question;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonbTransient
    public Student student;

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
            entity.student = studentResponse.student;
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


}
