package fr.istic.domain;

import java.time.Instant;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Version of a copy of the scan (T25e6b, ADR 0005): the copy starting at page {@code firstPage} of the scan (from 1)
 * was printed in version {@code label}. Written after the alignment, by the front, from the page codes read on the
 * copies; read by the statistics per version. A copy whose code could not be read has no row.
 */
@Entity
@Table(name = "exam_copy_version", uniqueConstraints = @UniqueConstraint(name = "ux_exam_copy_version_page", columnNames = { "exam_id", "first_page" }))
@RegisterForReflection
public class ExamCopyVersion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    public Exam exam;

    @Column(name = "first_page", nullable = false)
    public int firstPage;

    @Column(name = "label", nullable = false, length = 2)
    public String label;

    /** Print run and copy read on the page codes, when known. */
    @Column(name = "edition_id")
    public Long editionId;

    @Column(name = "copy_number")
    public Integer copyNumber;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public static List<ExamCopyVersion> findByExamId(long examId) {
        return find("exam.id = ?1 order by firstPage", examId).list();
    }

    public static long deleteByExamId(long examId) {
        return delete("exam.id = ?1", examId);
    }
}
