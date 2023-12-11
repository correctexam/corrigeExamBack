package fr.istic.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A HybridGradedComment.
 */
@Entity
@Table(name = "hybrid_graded_comment")
@Cacheable
@RegisterForReflection
public class HybridGradedComment extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "text")
    public String text;

    @Lob
    @Column(name = "description")
    public String description;

    @Column(name = "grade")
    public Integer grade;

    @Column(name = "relative")
    public Boolean relative;

    @Column(name = "step")
    public Integer step;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonbTransient
    public Question question;

    @OneToMany(mappedBy = "hybridcomments")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<Answer2HybridGradedComment> valueAnswers = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HybridGradedComment)) {
            return false;
        }
        return id != null && id.equals(((HybridGradedComment) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return (
            "HybridGradedComment{" +
            "id=" +
            id +
            ", text='" +
            text +
            "'" +
            ", description='" +
            description +
            "'" +
            ", grade=" +
            grade +
            ", relative='" +
            relative +
            "'" +
            ", step=" +
            step +
            "}"
        );
    }

    public HybridGradedComment update() {
        return update(this);
    }

    public HybridGradedComment persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static HybridGradedComment update(HybridGradedComment hybridGradedComment) {
        if (hybridGradedComment == null) {
            throw new IllegalArgumentException("hybridGradedComment can't be null");
        }
        var entity = HybridGradedComment.<HybridGradedComment>findById(hybridGradedComment.id);
        if (entity != null) {
            entity.text = hybridGradedComment.text;
            entity.description = hybridGradedComment.description;
            entity.grade = hybridGradedComment.grade;
            entity.relative = hybridGradedComment.relative;
            entity.step = hybridGradedComment.step;
            entity.question = hybridGradedComment.question;
            entity.valueAnswers = hybridGradedComment.valueAnswers;
        }
        return entity;
    }

    public static HybridGradedComment persistOrUpdate(HybridGradedComment hybridGradedComment) {
        if (hybridGradedComment == null) {
            throw new IllegalArgumentException("hybridGradedComment can't be null");
        }
        if (hybridGradedComment.id == null) {
            persist(hybridGradedComment);
            return hybridGradedComment;
        } else {
            return update(hybridGradedComment);
        }
    }


    public static PanacheQuery<HybridGradedComment> findByQuestionId( long qid) {
        return find("select hybridGradedComment from HybridGradedComment hybridGradedComment where hybridGradedComment.question.id =?1", qid);
    }

    public static PanacheQuery<HybridGradedComment> canAccess(long commentId, String login) {
        return find("select ex from HybridGradedComment ex join ex.question.exam.course.profs as u where ex.id =?1 and u.login =?2", commentId, login);
    }



    public static  long deleteByQIds( Set<Long> qids) {
        return delete("delete from HybridGradedComment sr where sr.question.id in ?1", qids);
    }
}
