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
import java.util.List;
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

    public static PanacheQuery<ExamSheet> findExamSheetByScanAndPageminAndPagemax(Long scanId, Integer pagemin,
            Integer pagemax) {
        return find("select e from ExamSheet e where e.scan.id =?1 and  e.pagemin = ?2 and e.pagemax = ?3", scanId,pagemin,pagemax);
    }


    public static PanacheQuery<ExamSheet> findExamSheetByScan( long scanId) {
        return find("select e from ExamSheet e where e.scan.id =?1", scanId);
    }

    public static PanacheQuery<ExamSheet> findExamSheetByScanWithoutMinusOne( long scanId) {
        return find("select e from ExamSheet e where e.scan.id =?1 and  e.pagemin <> -1 and e.pagemax <> -1", scanId);
    }


    public static PanacheQuery<ExamSheet> findExamSheetByScanAndStudentId( long scanId, long studentId) {
        return find("select e from ExamSheet e join e.students as st where e.scan.id =?1 and st.id = ?2", scanId, studentId);
    }

    public static PanacheQuery<ExamSheet> canAccess(long courseGroupId, String login) {
        return find("select ex from Exam ex join ex.scanfile as s join s.sheets as sh join ex.course.profs as u where  sh.id =?1 and u.login =?2", courseGroupId, login);
    }

    public static PanacheQuery<ExamSheet> getAll4ExamId( long examId) {
        return find("select distinct ex from Exam as e join  e.scanfile.sheets  as ex  where e.id = ?1 and  ex.pagemin <> -1 and ex.pagemax <> -1" ,examId);
    }

    public static PanacheQuery<ExamSheet> getAll4ExamIdEvenOrphan( long examId) {
        return find("select distinct ex from Exam as e join  e.scanfile.sheets  as ex  where e.id = ?1" ,examId);
    }


    public static PanacheQuery<ExamSheet> getAllOrphan4ExamId( long examId) {
        return find("select distinct ex from Exam as e join  e.scanfile.sheets  as ex  where e.id = ?1 and ( ( ex.pagemin = -1 and ex.pagemax = -1) or ex.students IS EMPTY)",examId);
    }

    public static PanacheQuery<ExamSheet> getAllDouble4Same( long examSheetId) {
        return find("select  ex from ExamSheet ex where ex.id = ?1 and EXISTS  (select 1 from ExamSheet ex1 WHERE ex1.id <> ex.id and ex1.pagemin = ex.pagemin and ex1.pagemax = ex.pagemax and ex1.scan.id = ex.scan.id)",examSheetId);
    }

    public static PanacheQuery<ExamSheet> getAll4ExamIdNotInStudentIdList( long examId, List<Long> sheetsid) {
        return find("select distinct ex from Exam as e join  e.scanfile.sheets  as ex join fetch ex.students  where e.id = ?1 and  ex.pagemin <> -1 and ex.pagemax <> -1 and ex.students IS NOT EMPTY and ex.id not IN ?2" ,examId,sheetsid);
    }





    public void cleanBeforDelete(){
        this.students.forEach(e-> {
            e.examSheets.remove(this);
            e.update();
        });
    }

}
