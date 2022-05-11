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
 * A ExamSheet.
 */
@Entity
@Table(name = "exam_sheet")
@Cacheable
@RegisterForReflection
public class ExamSheet extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "pagemin")
    public Integer pagemin;

    @Column(name = "pagemax")
    public Integer pagemax;

    @ManyToOne
    @JoinColumn(name = "scan_id")
    @JsonbTransient
    public Scan scan;

    @ManyToMany(mappedBy = "examSheets")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonbTransient
    public Set<Student> students = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExamSheet)) {
            return false;
        }
        return id != null && id.equals(((ExamSheet) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ExamSheet{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", pagemin=" + pagemin +
            ", pagemax=" + pagemax +
            "}";
    }

    public ExamSheet update() {
        return update(this);
    }

    public ExamSheet persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static ExamSheet update(ExamSheet examSheet) {
        if (examSheet == null) {
            throw new IllegalArgumentException("examSheet can't be null");
        }
        var entity = ExamSheet.<ExamSheet>findById(examSheet.id);
        if (entity != null) {
            entity.name = examSheet.name;
            entity.pagemin = examSheet.pagemin;
            entity.pagemax = examSheet.pagemax;
            entity.scan = examSheet.scan;
            entity.students = examSheet.students;
        }
        return entity;
    }

    public static ExamSheet persistOrUpdate(ExamSheet examSheet) {
        if (examSheet == null) {
            throw new IllegalArgumentException("examSheet can't be null");
        }
        if (examSheet.id == null) {
            persist(examSheet);
            return examSheet;
        } else {
            return update(examSheet);
        }
    }
    public static PanacheQuery<ExamSheet> findExamSheetByName( String name) {
        return find("select e from ExamSheet e where e.name =?1", name);
    }

    public static PanacheQuery<ExamSheet> findExamSheetByScan( long scanId) {
        return find("select e from ExamSheet e where e.scan.id =?1", scanId);
    }

    public static PanacheQuery<ExamSheet> findExamSheetByScanAndStudentId( long scanId, long studentId) {
        return find("select e from ExamSheet e join e.students as st where e.scan.id =?1 and st.id = ?2", scanId, studentId);
    }



}
