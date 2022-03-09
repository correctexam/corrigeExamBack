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
 * A CourseGroup.
 */
@Entity
@Table(name = "course_group")
@Cacheable
@RegisterForReflection
public class CourseGroup extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "group_name", nullable = false)
    public String groupName;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "course_group_students",
               joinColumns = @JoinColumn(name = "course_group_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "students_id", referencedColumnName = "id"))
    @JsonbTransient
    public Set<Student> students = new HashSet<>();

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
        if (!(o instanceof CourseGroup)) {
            return false;
        }
        return id != null && id.equals(((CourseGroup) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CourseGroup{" +
            "id=" + id +
            ", groupName='" + groupName + "'" +
            "}";
    }

    public CourseGroup update() {
        return update(this);
    }

    public CourseGroup persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static CourseGroup update(CourseGroup courseGroup) {
        if (courseGroup == null) {
            throw new IllegalArgumentException("courseGroup can't be null");
        }
        var entity = CourseGroup.<CourseGroup>findById(courseGroup.id);
        if (entity != null) {
            entity.groupName = courseGroup.groupName;
            entity.students = courseGroup.students;
            entity.course = courseGroup.course;
        }
        return entity;
    }

    public static CourseGroup persistOrUpdate(CourseGroup courseGroup) {
        if (courseGroup == null) {
            throw new IllegalArgumentException("courseGroup can't be null");
        }
        if (courseGroup.id == null) {
            persist(courseGroup);
            return courseGroup;
        } else {
            return update(courseGroup);
        }
    }

    public static PanacheQuery<CourseGroup> findAllWithEagerRelationships() {
        return find("select distinct courseGroup from CourseGroup courseGroup left join fetch courseGroup.students");
    }

    public static Optional<CourseGroup> findOneWithEagerRelationships(Long id) {
        return find("select courseGroup from CourseGroup courseGroup left join fetch courseGroup.students where courseGroup.id =?1", id).firstResultOptional();
    }

}
