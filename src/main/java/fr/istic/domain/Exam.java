package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Exam.
 */
@Entity
@Table(name = "exam")
@Cacheable
@RegisterForReflection
public class Exam extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    public String name;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(unique = true)
    public Template template;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(unique = true)
    public Zone idzone;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(unique = true)
    public Zone namezone;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(unique = true)
    public Zone firstnamezone;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(unique = true)
    public Zone notezone;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(unique = true)
    public Scan scanfile;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.REMOVE)
    //@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<Question> questions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonbTransient
    public Course course;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Exam)) {
            return false;
        }
        return id != null && id.equals(((Exam) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Exam{" +
            "id=" + id +
            ", name='" + name + "'" +
            "}";
    }

    public Exam update() {
        return update(this);
    }

    public Exam persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Exam update(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        var entity = Exam.<Exam>findById(exam.id);
        if (entity != null) {
            entity.name = exam.name;
            entity.template = exam.template;
            entity.idzone = exam.idzone;
            entity.namezone = exam.namezone;
            entity.firstnamezone = exam.firstnamezone;
            entity.notezone = exam.notezone;
            entity.scanfile = exam.scanfile;
            entity.questions = exam.questions;
            entity.course = exam.course;
        }
        return entity;
    }

    public static Exam persistOrUpdate(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        if (exam.id == null) {
            persist(exam);
            return exam;
        } else {
            return update(exam);
        }
    }

    @Transactional
    public static long removeNameZoneId(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        if (exam.id == null) {
            return 0;
        } else {
            return  update("namezone = ?1 where id = ?2", null, exam.id);

        }
    }
    @Transactional
    public static long removeFirstNameZoneId(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        if (exam.id == null) {
            return 0;
        } else {
            return  update("firstnamezone = ?1 where id = ?2", null, exam.id);

        }
    }
    @Transactional
    public static long removeIdZoneId(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        if (exam.id == null) {
            return 0;
        } else {
            return  update("idzone = ?1 where id = ?2", null, exam.id);

        }
    }
    @Transactional
    public static long removeNoteZoneId(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("exam can't be null");
        }
        if (exam.id == null) {
            return 0;
        } else {
            return  update("notezone = ?1 where id = ?2", null, exam.id);

        }
    }


    public static PanacheQuery<Exam> findExambyCourseId( long courseId) {
        return find("select exam from Exam exam where exam.course.id =?1", courseId);
    }
    public static PanacheQuery<Exam> findExambyScanId( long scanId) {
        return find("select exam from Exam exam where exam.scanfile.id =?1", scanId);
    }


    public static PanacheQuery<Exam> findExamThatMatchZoneId( long zoneId) {
        // join fetch exam.namezone join fetch exam.firstnamezone  join fetch exam.idzone  join fetch exam.notezone
        return find("select exam from Exam exam where exam.idzone.id =?1 or exam.namezone.id =?1 or exam.firstnamezone.id =?1 or exam.notezone.id =?1", zoneId);
    }

    public static PanacheQuery<Exam> canAccess( long examId, String login) {
        return find("select exam from Exam exam join exam.course.profs u where exam.id =?1 and u.login =?2", examId, login);
    }



}
