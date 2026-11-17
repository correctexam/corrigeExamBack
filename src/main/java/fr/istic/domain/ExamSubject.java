package fr.istic.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Typst subject of an exam (T25c, ADR 0004): metadata only. The files are in the object storage under
 * {@code subject/{examId}/files/}; the database keeps their list, size and hash.
 */
@Entity
@Table(name = "exam_subject")
@RegisterForReflection
public class ExamSubject extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "exam_id", nullable = false, unique = true)
    public Exam exam;

    /** File compiled first, e.g. {@code main.typ}. */
    @Column(name = "main_file", nullable = false, length = 200)
    public String mainFile = "main.typ";

    /** Incremented by every change of the files: lets the editor detect a concurrent change. */
    @Column(name = "revision", nullable = false)
    public int revision;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "updated_by", length = 50)
    public String updatedBy;

    @Column(name = "locked_by", length = 50)
    public String lockedBy;

    @Column(name = "lock_expires_at")
    public Instant lockExpiresAt;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("path")
    public List<ExamSubjectFile> files = new ArrayList<>();

    public static Optional<ExamSubject> findByExamId(long examId) {
        return find("exam.id", examId).firstResultOptional();
    }

    public Optional<ExamSubjectFile> file(String path) {
        return files.stream().filter(f -> f.path.equals(path)).findFirst();
    }
}
