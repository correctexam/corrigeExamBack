package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A GradedComment.
 */
@Entity
@Table(name = "graded_comment")
@RegisterForReflection
public class GradedComment extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "zonegeneratedid")
    public String zonegeneratedid;

    @Column(name = "text")
    public String text;

    @Lob
    @Column(name = "description")
     public String description;

    @Column(name = "grade")
    public Integer gradequarter;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonbTransient
    public Question question;

    @ManyToMany(mappedBy = "gradedcomments")
    @JsonbTransient
    public Set<StudentResponse> studentResponses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GradedComment)) {
            return false;
        }
        return id != null && id.equals(((GradedComment) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "GradedComment{" +
            "id=" + id +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            ", text='" + text + "'" +
            ", description='" + description + "'" +
            ", grade=" + gradequarter +
            "}";
    }

    public GradedComment update() {
        return update(this);
    }

    public GradedComment persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static GradedComment update(GradedComment gradedComment) {
        if (gradedComment == null) {
            throw new IllegalArgumentException("gradedComment can't be null");
        }
        var entity = GradedComment.<GradedComment>findById(gradedComment.id);
        if (entity != null) {
            entity.zonegeneratedid = gradedComment.zonegeneratedid;
            entity.text = gradedComment.text;
            entity.description = gradedComment.description;
            entity.gradequarter = gradedComment.gradequarter;
            entity.question = gradedComment.question;
            entity.studentResponses = gradedComment.studentResponses;
        }
        return entity;
    }

    public static GradedComment persistOrUpdate(GradedComment gradedComment) {
        if (gradedComment == null) {
            throw new IllegalArgumentException("gradedComment can't be null");
        }
        if (gradedComment.id == null) {
            persist(gradedComment);
            return gradedComment;
        } else {
            return update(gradedComment);
        }
    }

    public static PanacheQuery<GradedComment> findByQuestionId( long qid) {
        return find("select gradedcomment from GradedComment gradedcomment where gradedcomment.question.id =?1", qid);
    }

    public static PanacheQuery<GradedComment> canAccess(long commentId, String login) {
        return find("select ex from GradedComment ex join ex.question.exam.course.profs as u where ex.id =?1 and u.login =?2", commentId, login);
    }

}
