package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

/**
 * A Zone.
 */
@Entity
@Table(name = "zone")
@Cacheable
@RegisterForReflection
public class Zone extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "page_number")
    public Integer pageNumber;

    @Column(name = "x_init")
    public Integer xInit;

    @Column(name = "y_init")
    public Integer yInit;

    @Column(name = "width")
    public Integer width;

    @Column(name = "height")
    public Integer height;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Zone)) {
            return false;
        }
        return id != null && id.equals(((Zone) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Zone{" +
            "id=" + id +
            ", pageNumber=" + pageNumber +
            ", xInit=" + xInit +
            ", yInit=" + yInit +
            ", width=" + width +
            ", height=" + height +
            "}";
    }

    public Zone update() {
        return update(this);
    }

    public Zone persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Zone update(Zone zone) {
        if (zone == null) {
            throw new IllegalArgumentException("zone can't be null");
        }
        var entity = Zone.<Zone>findById(zone.id);
        if (entity != null) {
            entity.pageNumber = zone.pageNumber;
            entity.xInit = zone.xInit;
            entity.yInit = zone.yInit;
            entity.width = zone.width;
            entity.height = zone.height;
        }
        return entity;
    }

    public static Zone persistOrUpdate(Zone zone) {
        if (zone == null) {
            throw new IllegalArgumentException("zone can't be null");
        }
        if (zone.id == null) {
            persist(zone);
            return zone;
        } else {
            return update(zone);
        }
    }

    public static PanacheQuery<Zone> getQuestionZone4ExamId(long examId) {
        return find("select q.zone from Question q where q.exam.id =?1", examId);
    }


    public static long deleteAllZonesIds(Set<Long> zonesId) {
        return delete("delete from Zone z where z.id in ?1", zonesId);
    }

    public static PanacheQuery<Zone> canAccess1(long zoneId, String login) {
        return find("select q.zone from Question q join q.exam.course.profs as u where q.zone.id =?1 and u.login =?2", zoneId, login);
    }
    public static PanacheQuery<Zone> canAccess2(long zoneId, String login) {
        return find("select e from Exam e join e.course.profs as u where (e.firstnamezone.id =?1 or e.idzone.id =?1 or e.namezone.id =?1)  and u.login =?2", zoneId, login);
    }

    public static PanacheQuery<StudentResponse> getNumberOfStudentResponse4Zone(Long zoneid) {
        return find("select sr from StudentResponse sr where sr.question.zone.id=?1", zoneid);
    }




}
