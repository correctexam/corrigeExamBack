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
 * A Student.
 */
@Entity
@Table(name = "student")
@RegisterForReflection
public class Student extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "firstname")
    public String firstname;

    @NotNull
    @Column(name = "ine", nullable = false)
    public String ine;

    @Column(name = "caslogin")
    public String caslogin;

    @Column(name = "mail")
    public String mail;

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "student_exam_sheets",
               joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "exam_sheets_id", referencedColumnName = "id"))
    @JsonbTransient
    public Set<ExamSheet> examSheets = new HashSet<>();

    @ManyToMany(mappedBy = "students")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonbTransient
    public Set<CourseGroup> groups = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Student)) {
            return false;
        }
        return id != null && id.equals(((Student) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Student{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", firstname='" + firstname + "'" +
            ", ine='" + ine + "'" +
            ", caslogin='" + caslogin + "'" +
            ", mail='" + mail + "'" +
            "}";
    }

    public Student update() {
        return update(this);
    }

    public Student persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Student update(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("student can't be null");
        }
        var entity = Student.<Student>findById(student.id);
        if (entity != null) {
            entity.name = student.name;
            entity.firstname = student.firstname;
            entity.ine = student.ine;
            entity.caslogin = student.caslogin;
            entity.mail = student.mail;
            entity.examSheets = student.examSheets;
            entity.groups = student.groups;
        }
        return entity;
    }

    public static Student persistOrUpdate(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("student can't be null");
        }
        if (student.id == null) {
            persist(student);
            return student;
        } else {
            return update(student);
        }
    }

    public static PanacheQuery<Student> findAllWithEagerRelationships() {
        return find("select distinct student from Student student left join fetch student.examSheets");
    }

    public static Optional<Student> findOneWithEagerRelationships(Long id) {
        return find("select student from Student student left join fetch student.examSheets where student.id =?1", id).firstResultOptional();
    }

    public static PanacheQuery<Student> findStudentsbyCourseId( long courseId) {
        return find("select distinct student from Student student LEFT join student.examSheets join student.groups as g  where g.course.id =?1", courseId);
     //return find("select distinct e.students from ExamSheet as e join e.students as s join s.groups as g  where g.course.id =?1", courseId);

    }
    public static PanacheQuery<Student> findStudentsbyCourseIdAndINE( long courseId, String ine) {
        return find("select distinct student from Student student join student.groups as g where g.course.id =?1 and student.ine = ?2", courseId, ine);
    }
    public static PanacheQuery<Student> findStudentsbyCourseIdAndFirsNameAndLastName( long courseId, String firstname, String lastname) {
        return find("select distinct student from Student student join student.groups as g where g.course.id =?1 and student.firstname = ?2 and student.name = ?3", courseId, firstname, lastname);
    }


    public static PanacheQuery<Student> findStudentsbySheetId( long sheetId) {
        return find("select distinct student from Student student join student.examSheets as e where e.id =?1", sheetId);
    }

    public static PanacheQuery<Student> canAccess(long studentId, String login) {
        return find("select s from Student s join s.groups as c join c.course.profs as u where s.id =?1 and u.login =?2", studentId, login);
    }


}
