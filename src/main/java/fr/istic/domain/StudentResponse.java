package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.persistence.*;
import javax.transaction.Transactional;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * A StudentResponse.
 */
@Entity
@Table(name = "student_response")
@Cacheable
@RegisterForReflection
public class StudentResponse extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "note")
    public Integer note;

    @Column(name = "star")
    public Boolean star;

    @Column(name = "worststar")
    public Boolean worststar;

    @OneToMany(mappedBy = "studentResponse")
    public Set<Comments> comments = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonbTransient
    public Question question;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    @JsonbTransient
    public ExamSheet sheet;

    @ManyToMany(cascade = CascadeType.REMOVE)
//    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "student_response_textcomments",
               joinColumns = @JoinColumn(name = "student_response_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "textcomments_id", referencedColumnName = "id"))
    @JsonbTransient
    public Set<TextComment> textcomments = new HashSet<>();

    @ManyToMany
//    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "student_response_gradedcomments",
               joinColumns = @JoinColumn(name = "student_response_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "gradedcomments_id", referencedColumnName = "id"))
    @JsonbTransient
    public Set<GradedComment> gradedcomments = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StudentResponse)) {
            return false;
        }
        return id != null && id.equals(((StudentResponse) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "StudentResponse{" +
            "id=" + id +
            ", note=" + note +
            ", star='" + star + "'" +
            ", worststar='" + worststar + "'" +
            "}";
    }

    public StudentResponse update() {
        return update(this);
    }

    public StudentResponse persistOrUpdate() {
        return persistOrUpdate(this);
    }


    public void clearComments(){
        this.gradedcomments.clear();
        this.textcomments.clear();
        this.persistOrUpdate();
    }

    @Override
    public void delete() {
        super.delete();
    }

    public static StudentResponse update(StudentResponse studentResponse) {
        if (studentResponse == null) {
            throw new IllegalArgumentException("studentResponse can't be null");
        }
        var entity = StudentResponse.<StudentResponse>findById(studentResponse.id);
        if (entity != null) {
            entity.note = studentResponse.note;
            entity.star = studentResponse.star;
            entity.worststar = studentResponse.worststar;
            entity.comments = studentResponse.comments;
            entity.question = studentResponse.question;
            entity.sheet = studentResponse.sheet;

            var ts  = studentResponse.textcomments.stream().map(te -> te.id).collect(Collectors.toList());
            entity.textcomments.removeIf(t -> !ts.contains(t.id));
            var ts1  = entity.textcomments.stream().map(te -> te.id).collect(Collectors.toList());
            entity.textcomments.addAll(studentResponse.textcomments.stream().filter(ts2 -> !ts1.contains(ts2.id)).collect(Collectors.toList()));

            var gs  = studentResponse.gradedcomments.stream().map(te -> te.id).collect(Collectors.toList());
            entity.gradedcomments.removeIf(t -> !gs.contains(t.id));
            var gs1  = entity.gradedcomments.stream().map(te -> te.id).collect(Collectors.toList());
            entity.gradedcomments.addAll(studentResponse.gradedcomments.stream().filter(gs2 -> !gs1.contains(gs2.id)).collect(Collectors.toList()));
        }
        return entity;
    }

    public static StudentResponse persistOrUpdate(StudentResponse studentResponse) {
        if (studentResponse == null) {
            throw new IllegalArgumentException("studentResponse can't be null");
        }
        if (studentResponse.id == null) {
            persist(studentResponse);
            return studentResponse;
        } else {
            return update(studentResponse);
        }
    }


    public static PanacheQuery<StudentResponse> findAllWithEagerRelationships() {
        return find("select distinct studentResponse from StudentResponse studentResponse left join fetch studentResponse.textcomments left join fetch studentResponse.gradedcomments");
    }

    public static Optional<StudentResponse> findOneWithEagerRelationships(Long id) {
        return find("select studentResponse from StudentResponse studentResponse left join fetch studentResponse.textcomments left join fetch studentResponse.gradedcomments where studentResponse.id =?1", id).firstResultOptional();
    }

    public static PanacheQuery<StudentResponse> findStudentResponsesbysheetIdAndquestionId( long sheetId, long questionId) {
        return find("select sr from StudentResponse sr where sr.sheet.id =?1 and question.id=?2", sheetId, questionId);
    }
    public static PanacheQuery<StudentResponse> findStudentResponsesbysheetId( long sheetId) {
        return find("select sr from StudentResponse sr where sr.sheet.id =?1 ", sheetId );
    }

    public static PanacheQuery<StudentResponse> getAll4ExamId( long examId) {
        return find("select sr from StudentResponse sr where  sr.question.exam.id = ?1",examId);
    }

    public static PanacheQuery<ExamSheet> getBestAnswerforQuestionNoAndExamId( long examId, int questionNo) {
        return find("select distinct sr.sheet from StudentResponse sr where sr.question.numero = ?2 and  sr.question.exam.id = ?1 and sr.star = true",examId,questionNo );
    }

    public static PanacheQuery<StudentResponse> getAllBestAnswerforExamId( long examId) {
        return find("select distinct sr from StudentResponse sr join fetch sr.question join fetch  sr.sheet where sr.question.exam.id = ?1 and sr.star = true",examId );
    }

    public static PanacheQuery<StudentResponse> getAllWorstAnswerforExamId( long examId) {
        return find("select distinct sr from StudentResponse sr join fetch sr.question  join fetch  sr.sheet where  sr.question.exam.id = ?1 and sr.worststar = true",examId );
    }


    public static PanacheQuery<StudentResponse> canAccess(long srId, String login) {
        return find("select ex from StudentResponse ex join ex.question.exam.course.profs as u where ex.id =?1 and u.login =?2", srId, login);
    }
}
