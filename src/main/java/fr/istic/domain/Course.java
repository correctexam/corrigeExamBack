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
import java.util.Optional;

/**
 * A Course.
 */
@Entity
@Table(name = "course")
@Cacheable
@RegisterForReflection
public class Course extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    public String name;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<Exam> exams = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    public Set<CourseGroup> groups = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @NotNull
    @JoinTable(name = "course_prof",
               joinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "prof_id", referencedColumnName = "id"))
    @JsonbTransient
    public Set<User> profs = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course)) {
            return false;
        }
        return id != null && id.equals(((Course) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Course{" +
            "id=" + id +
            ", name='" + name + "'" +
            "}";
    }

    public Course update() {
        return update(this);
    }

    public Course persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Course update(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("course can't be null");
        }
        var entity = Course.<Course>findById(course.id);
        if (entity != null) {
            entity.name = course.name;
            entity.exams = course.exams;
            entity.groups = course.groups;
            entity.profs = course.profs;
        }
        return entity;
    }

    public static Course persistOrUpdate(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("course can't be null");
        }
        if (course.id == null) {
            persist(course);
            return course;
        } else {
            return update(course);
        }
    }


    public static PanacheQuery<Course> findAllWithEagerRelationships() {
        return find("select distinct course from Course course left join fetch course.profs");
    }

    public static Optional<Course> findOneWithEagerRelationships(Long id) {
        return find("select course from Course course left join fetch course.profs where course.id =?1", id).firstResultOptional();
    }


    public static PanacheQuery<Course> findByProfIsCurrentUser( String login) {
        return find("select course from Course course join course.profs as u where u.login =?1", login);
    }

    public static PanacheQuery<Course> canAccess(long courseId, String login) {
        return find("select course from Course course join course.profs as u where u.login =?1 and course.id = ?2", login, courseId);
    }


}
