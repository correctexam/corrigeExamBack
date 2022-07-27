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
 * A Scan.
 */
@Entity
@Table(name = "scan")
@Cacheable
@RegisterForReflection
public class Scan extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    public String name;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content")
    public byte[] content;

    @Column(name = "content_content_type")
      public String contentContentType;

    @OneToMany(mappedBy = "scan",cascade = CascadeType.REMOVE)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<ExamSheet> sheets = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Scan)) {
            return false;
        }
        return id != null && id.equals(((Scan) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Scan{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", content='" + content + "'" +
            ", contentContentType='" + contentContentType + "'" +
            "}";
    }

    public Scan update() {
        return update(this);
    }

    public Scan persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Scan update(Scan scan) {
        if (scan == null) {
            throw new IllegalArgumentException("scan can't be null");
        }
        var entity = Scan.<Scan>findById(scan.id);
        if (entity != null) {
            entity.name = scan.name;
            entity.content = scan.content;
            entity.sheets = scan.sheets;
        }
        return entity;
    }

    public static Scan persistOrUpdate(Scan scan) {
        if (scan == null) {
            throw new IllegalArgumentException("scan can't be null");
        }
        if (scan.id == null) {
            persist(scan);
            return scan;
        } else {
            return update(scan);
        }
    }

    public static PanacheQuery<Scan> findByName( String name) {
        return find("select scanfile from Scan scanfile where scanfile.name =?1", name);
    }


    public static PanacheQuery<Scan> canAccess( long scanId, String login) {
        return find("select exam.scanfile from Exam exam join exam.course.profs as u where exam.scanfile.id =?1 and u.login =?2", scanId, login);
    }

}
