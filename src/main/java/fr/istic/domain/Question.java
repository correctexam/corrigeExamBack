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

/**
 * A Question.
 */
@Entity
@Table(name = "question")
@Cacheable
@RegisterForReflection
public class Question extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "numero", nullable = false)
    public Integer numero;

    @Column(name = "point")
    public Integer point;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(unique = true)
    public Zone zone;

    @ManyToOne
    @JoinColumn(name = "type_id")
    @JsonbTransient
    public QuestionType type;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    @JsonbTransient
    public Exam exam;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Question)) {
            return false;
        }
        return id != null && id.equals(((Question) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Question{" +
            "id=" + id +
            ", numero=" + numero +
            ", point=" + point +
            "}";
    }

    public Question update() {
        return update(this);
    }

    public Question persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Question update(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("question can't be null");
        }
        var entity = Question.<Question>findById(question.id);
        if (entity != null) {
            entity.numero = question.numero;
            entity.point = question.point;
            entity.zone = question.zone;
            entity.type = question.type;
            entity.exam = question.exam;
        }
        return entity;
    }

    public static Question persistOrUpdate(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("question can't be null");
        }
        if (question.id == null) {
            persist(question);
            return question;
        } else {
            return update(question);
        }
    }


}
