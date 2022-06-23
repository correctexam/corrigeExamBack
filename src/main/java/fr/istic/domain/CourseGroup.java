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
import java.util.stream.Collectors;
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

    @ManyToMany(fetch = FetchType.LAZY)
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
            // entity.students = courseGroup.students;
            var ts  = courseGroup.students.stream().map(te -> te.id).collect(Collectors.toList());
            entity.students.removeIf(t -> !ts.contains(t.id));
            var ts1  = entity.students.stream().map(te -> te.id).collect(Collectors.toList());
            entity.students.addAll(courseGroup.students.stream().filter(ts2 -> !ts1.contains(ts2.id)).collect(Collectors.toList()));
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

    public static PanacheQuery<CourseGroup> findByNameandCourse( long courseId, String name) {
        return find("select c from CourseGroup c where c.course.id =?1 and c.groupName = ?2", courseId, name);
    }

    public static PanacheQuery<CourseGroup> findByStudentINEandCourse( long courseId, String ine) {
        return find("select c from CourseGroup c join c.students s where c.course.id =?1 and s.ine = ?2", courseId, ine);
    }


    public static PanacheQuery<CourseGroup> canAccess(long courseGroupId, String login) {
        return find("select c from CourseGroup c join c.course.profs u where c.id =?1 and u.login =?2", courseGroupId, login);
    }


}
