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

/**
 * Print run of an exam written in Typst (T25d, ADR 0004): {@code copies} copies of the template, each page carrying
 * the code {@code CE1|<edition id>|<copy>|<page>}. The PDF itself is not stored: it is made again from the template
 * when needed. The scan reads the codes and checks them against the known print runs.
 */
@Entity
@Table(name = "exam_edition")
@RegisterForReflection
public class ExamEdition extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    public Exam exam;

    /** Copies numbered 1 to {@code copies}. */
    @Column(name = "copies", nullable = false)
    public int copies;

    /** Pages of one copy (pages of the template). */
    @Column(name = "pages_per_copy", nullable = false)
    public int pagesPerCopy;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "created_by", length = 50)
    public String createdBy;

    public static List<ExamEdition> findByExamId(long examId) {
        return find("exam.id = ?1 order by id", examId).list();
    }

    public static long deleteByExamId(long examId) {
        return delete("exam.id = ?1", examId);
    }
}
