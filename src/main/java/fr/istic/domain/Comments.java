package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 * A Comments.
 */
@Entity
@Table(name = "comments")
@Cacheable
@RegisterForReflection
public class Comments extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "zonegeneratedid")
    public String zonegeneratedid;

    @Lob
    @Column(name = "json_data")
    public String jsonData;

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
        if (!(o instanceof Comments)) {
            return false;
        }
        return id != null && id.equals(((Comments) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Comments{" +
            "id=" + id +
            ", zonegeneratedid='" + zonegeneratedid + "'" +
            ", jsonData='" + jsonData + "'" +
            "}";
    }

    public Comments update() {
        return update(this);
    }

    public Comments persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Comments update(Comments comments) {
        if (comments == null) {
            throw new IllegalArgumentException("comments can't be null");
        }
        var entity = Comments.<Comments>findById(comments.id);
        if (entity != null) {
            entity.zonegeneratedid = comments.zonegeneratedid;
            entity.jsonData = comments.jsonData;
            entity.studentResponse = comments.studentResponse;
        }
        return entity;
    }

    public static Comments persistOrUpdate(Comments comments) {
        if (comments == null) {
            throw new IllegalArgumentException("comments can't be null");
        }
        if (comments.id == null) {
            persist(comments);
            return comments;
        } else {
            return update(comments);
        }
    }

    public static PanacheQuery<Comments> findCommentsbyZonegeneratedid( String zonegeneratedid) {
        return find("select c from Comments c where c.zonegeneratedid =?1", zonegeneratedid);
    }

    public static PanacheQuery<Comments> canAccess(long commentId, List<String> examIdList) {
        return find("select c from Comments c where c.id =?1 and SUBSTRING(c.zonegeneratedid, 1, LOCATE('_', c.zonegeneratedid) -1) in ?2", commentId, examIdList);
    }

    public static long deleteCommentByExamId( String examId) {
        return delete("delete from Comments as c where SUBSTRING(c.zonegeneratedid, 1, LOCATE('_', c.zonegeneratedid) -1) =?1",examId);
    }


}
