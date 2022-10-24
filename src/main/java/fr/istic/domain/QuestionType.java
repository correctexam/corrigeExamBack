package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A QuestionType.
 */
@Entity
@Table(name = "question_type")
@Cacheable
@RegisterForReflection
public class QuestionType extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "algo_name", nullable = false)
    public String algoName;

    @Column(name = "endpoint")
    public String endpoint;

    @Column(name = "js_function")
    public String jsFunction;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuestionType)) {
            return false;
        }
        return id != null && id.equals(((QuestionType) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "QuestionType{" +
            "id=" + id +
            ", algoName='" + algoName + "'" +
            ", endpoint='" + endpoint + "'" +
            ", jsFunction='" + jsFunction + "'" +
            "}";
    }

    public QuestionType update() {
        return update(this);
    }

    public QuestionType persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static QuestionType update(QuestionType questionType) {
        if (questionType == null) {
            throw new IllegalArgumentException("questionType can't be null");
        }
        var entity = QuestionType.<QuestionType>findById(questionType.id);
        if (entity != null) {
            entity.algoName = questionType.algoName;
            entity.endpoint = questionType.endpoint;
            entity.jsFunction = questionType.jsFunction;
        }
        return entity;
    }

    public static QuestionType persistOrUpdate(QuestionType questionType) {
        if (questionType == null) {
            throw new IllegalArgumentException("questionType can't be null");
        }
        if (questionType.id == null) {
            persist(questionType);
            return questionType;
        } else {
            return update(questionType);
        }
    }


}
