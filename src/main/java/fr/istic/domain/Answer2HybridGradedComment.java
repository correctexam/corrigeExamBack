package fr.istic.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Answer2HybridGradedComment.
 */
@Entity
@Table(name = "answer_2_hybrid_graded_comment")
@Cacheable
@RegisterForReflection
public class Answer2HybridGradedComment extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "step_value")
    public Integer stepValue;

    @ManyToOne
    @JoinColumn(name = "hybridcomments_id")
    @JsonbTransient
    public HybridGradedComment hybridcomments;

    @ManyToOne
    @JoinColumn(name = "student_response_id")
    @JsonbTransient
    public StudentResponse studentResponse;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Answer2HybridGradedComment)) {
            return false;
        }
        return id != null && id.equals(((Answer2HybridGradedComment) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Answer2HybridGradedComment{" + "id=" + id + ", stepValue=" + stepValue + "}";
    }

    public Answer2HybridGradedComment update() {
        return update(this);
    }

    public Answer2HybridGradedComment persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Answer2HybridGradedComment update(Answer2HybridGradedComment answer2HybridGradedComment) {
        if (answer2HybridGradedComment == null) {
            throw new IllegalArgumentException("answer2HybridGradedComment can't be null");
        }
        var entity = Answer2HybridGradedComment.<Answer2HybridGradedComment>findById(answer2HybridGradedComment.id);
        if (entity != null) {
            entity.stepValue = answer2HybridGradedComment.stepValue;
            entity.hybridcomments = answer2HybridGradedComment.hybridcomments;
            entity.studentResponse = answer2HybridGradedComment.studentResponse;
        }
        return entity;
    }

    public static Answer2HybridGradedComment persistOrUpdate(Answer2HybridGradedComment answer2HybridGradedComment) {
        if (answer2HybridGradedComment == null) {
            throw new IllegalArgumentException("answer2HybridGradedComment can't be null");
        }
        if (answer2HybridGradedComment.id == null) {
            persist(answer2HybridGradedComment);
            return answer2HybridGradedComment;
        } else {
            return update(answer2HybridGradedComment);
        }
    }

    public static PanacheQuery<Answer2HybridGradedComment> findAllWithResponseIdAndHybridCommentId( long responseId, long hybridCommentId) {
        return find("select c from Answer2HybridGradedComment c where c.studentResponse.id =?1 and c.hybridcomments.id =?2", responseId, hybridCommentId);
    }

    public static PanacheQuery<Answer2HybridGradedComment> findAllAnswerHybridGradedCommentByAnswerId(long responseId){
        return find("select c from Answer2HybridGradedComment c where c.studentResponse.id =?1", responseId);
    }
    public static PanacheQuery<Answer2HybridGradedComment> findAllAnswerHybridGradedCommentByCommentIdWithStepvalueUpperThan0(long hybridcommentId){
        return find("select c from Answer2HybridGradedComment c where c.hybridcomments.id =?1 and c.stepValue >0", hybridcommentId);
    }

    public static PanacheQuery<Answer2HybridGradedComment> findAllAnswerHybridGradedCommentByCommentId(long hybridcommentId){
        return find("select c from Answer2HybridGradedComment c where c.hybridcomments.id =?1", hybridcommentId);
    }

    public static PanacheQuery<Answer2HybridGradedComment> findAllAnswerHybridGradedCommentByCommentIdWithFetchWithStepvalueUpperThan0(long hybridCommentId){
        return find("select c from Answer2HybridGradedComment c join fetch c.studentResponse st join fetch st.hybridcommentsValues ah2 join fetch ah2.hybridcomments as h2 where c.hybridcomments.id  =?1 and c.stepValue >0", hybridCommentId);
    }

    public static PanacheQuery<Answer2HybridGradedComment> canAccess(long commentId, String login) {
        return find("select ex from Answer2HybridGradedComment ex join ex.hybridcomments.question.exam.course.profs as u where ex.id =?1 and u.login =?2", commentId, login);
    }

    public static long deleteAllAnswerHybridGradedCommentByCommentId(long  commentId){
        return delete("hybridcomments.id", commentId);
    }
    public static long deleteAllAnswerHybridGradedCommentByAnswerId(long  responseId){
        return delete("studentResponse.id", responseId);
    }



}
