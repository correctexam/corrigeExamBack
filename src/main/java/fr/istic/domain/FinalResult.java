package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;

/**
 * A FinalResult.
 */
@Entity
@Table(name = "final_result")
@Cacheable
@RegisterForReflection
public class FinalResult extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "note")
    public Integer note;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonbTransient
    public Student student;

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
        if (!(o instanceof FinalResult)) {
            return false;
        }
        return id != null && id.equals(((FinalResult) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "FinalResult{" +
            "id=" + id +
            ", note=" + note +
            "}";
    }

    public FinalResult update() {
        return update(this);
    }

    public FinalResult persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static FinalResult update(FinalResult finalResult) {
        if (finalResult == null) {
            throw new IllegalArgumentException("finalResult can't be null");
        }
        var entity = FinalResult.<FinalResult>findById(finalResult.id);
        if (entity != null) {
            entity.note = finalResult.note;
            entity.student = finalResult.student;
            entity.exam = finalResult.exam;
        }
        return entity;
    }

    public static FinalResult persistOrUpdate(FinalResult finalResult) {
        if (finalResult == null) {
            throw new IllegalArgumentException("finalResult can't be null");
        }
        if (finalResult.id == null) {
            persist(finalResult);
            return finalResult;
        } else {
            return update(finalResult);
        }
    }

    public static PanacheQuery<FinalResult> findFinalResultByStudentIdAndExamId( long studentId, long examId) {
        return find("select e from FinalResult e where e.student.id =?1 and e.exam.id =?2", studentId,examId);
    }

    public static PanacheQuery<FinalResult> canAccess(long finalResultId, String login) {
        return find("select ex from FinalResult ex where ex.id =?1 and ex.exam.course.prof.login =?2", finalResultId, login);
    }

}
