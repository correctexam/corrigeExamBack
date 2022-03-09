package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Exam.
 */
@Entity
@Table(name = "exam")
@Cacheable
@RegisterForReflection
public class Exam extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    public String name;

    @OneToOne
    @JoinColumn(unique = true)
    public Template template;

    @OneToOne
    @JoinColumn(unique = true)
    public Zone idzone;

    @OneToOne
    @JoinColumn(unique = true)
    public Zone namezone;

    @OneToOne
    @JoinColumn(unique = true)
    public Zone firstnamezone;

    @OneToOne
    @JoinColumn(unique = true)
    public Zone notezone;

    @OneToOne
    @JoinColumn(unique = true)
    public Scan scanfile;

    @OneToMany(mappedBy = "exam")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<Question> questions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonbTransient
    public Course course;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Exam)) {
            return false;
        }
        return id != null && id.equals(((Exam) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Exam{" +
            "id=" + id +
            ", name='" + name + "'" +
            "}";
    }

    public Exam update() {
        return update(this);
    }

    public Exam persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Exam update(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        var entity = Exam.<Exam>findById(exam.id);
        if (entity != null) {
            entity.name = exam.name;
            entity.template = exam.template;
            entity.idzone = exam.idzone;
            entity.namezone = exam.namezone;
            entity.firstnamezone = exam.firstnamezone;
            entity.notezone = exam.notezone;
            entity.scanfile = exam.scanfile;
            entity.questions = exam.questions;
            entity.course = exam.course;
        }
        return entity;
    }

    public static Exam persistOrUpdate(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        if (exam.id == null) {
            persist(exam);
            return exam;
        } else {
            return update(exam);
        }
    }


}
