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
 * A TextComment.
 */
@Entity
@Table(name = "text_comment")
@RegisterForReflection
public class TextComment extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "text")
    public String text;

    @Lob
    @Column(name = "description")
     public String description;

    @Column(name = "zonegeneratedid")
    public String zonegeneratedid;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonbTransient
    public Question question;

    @ManyToMany(mappedBy = "textcomments")
    @JsonbTransient
    public Set<StudentResponse> studentResponses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TextComment)) {
            return false;
        }
        return id != null && id.equals(((TextComment) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "TextComment{" +
            "id=" + id +
            ", text='" + text + "'" +
            ", description='" + description + "'" +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            "}";
    }

    public TextComment update() {
        return update(this);
    }

    public TextComment persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static TextComment update(TextComment textComment) {
        if (textComment == null) {
            throw new IllegalArgumentException("textComment can't be null");
        }
        var entity = TextComment.<TextComment>findById(textComment.id);
        if (entity != null) {
            entity.text = textComment.text;
            entity.description = textComment.description;
            entity.zonegeneratedid = textComment.zonegeneratedid;
            entity.question = textComment.question;
            entity.studentResponses = textComment.studentResponses;
        }
        return entity;
    }

    public static TextComment persistOrUpdate(TextComment textComment) {
        if (textComment == null) {
            throw new IllegalArgumentException("textComment can't be null");
        }
        if (textComment.id == null) {
            persist(textComment);
            return textComment;
        } else {
            return update(textComment);
        }
    }


    public static PanacheQuery<TextComment> findByQuestionId( long qid) {
        return find("select textcomment from TextComment textcomment where textcomment.question.id =?1", qid);
    }



}
