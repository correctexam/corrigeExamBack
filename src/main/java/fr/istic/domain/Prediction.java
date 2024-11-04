package fr.istic.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.json.bind.annotation.JsonbTransient;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Prediction.
 */
@Entity
@Table(name = "prediction")
@RegisterForReflection
public class Prediction extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "text")
    public String text;

    @Lob
    @Column(name = "json_data")
    public String jsonData;

    @Column(name = "zonegeneratedid")
    public String zonegeneratedid;

    @Column(name = "question_number")
    public String questionNumber;

    @Column(name = "student_id")
    public String studentId;

    @Column(name = "exam_id")
    public Long examId;


    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonbTransient
    public Question question;

    @ManyToMany(mappedBy = "predictions")
    @JsonbTransient
    public Set<StudentResponse> studentResponses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Prediction)) {
            return false;
        }
        return id != null && id.equals(((Prediction) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Prediction{" +
            "id=" + id +
            ", text='" + text + "'" +
            ", jsonData='" + jsonData + "'" +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            ", questionNumber='" + questionNumber + "'" +
            ", examId='" + examId + "'" +
            ", studentId='" + studentId + "'" +
            "}";
    }

    public Prediction update() {
        return update(this);
    }

    public Prediction persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Prediction update(Prediction prediction) {
        if (prediction == null) {
            throw new IllegalArgumentException("prediction can't be null");
        }
        var entity = Prediction.<Prediction>findById(prediction.id);
        if (entity != null) {
            entity.text = prediction.text;
            entity.jsonData = prediction.jsonData;
            entity.zonegeneratedid = prediction.zonegeneratedid;
            entity.questionNumber = prediction.questionNumber;
            entity.question = prediction.question;
            entity.studentResponses = prediction.studentResponses;
        }
        return entity;
    }

    public static Prediction persistOrUpdate(Prediction prediction) {
        if (prediction == null) {
            throw new IllegalArgumentException("prediction can't be null");
        }
        if (prediction.id == null) {
            persist(prediction);
            return prediction;
        } else {
            return update(prediction);
        }
    }

    public static PanacheQuery<Prediction> findByQuestionId(long qid) {
        return find("select prediction from Prediction prediction where prediction.question.id = ?1", qid);
    }

    public static long deleteByQIds(Set<Long> qids) {
        return delete("delete from Prediction pr where pr.question.id in ?1", qids);
    }

    public static PanacheQuery<Prediction> canAccess(long predictionId, String login) {
        return find("select pr from Prediction pr join pr.question.exam.course.profs as u where pr.id = ?1 and u.login = ?2", predictionId, login);
    }
}
