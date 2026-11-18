package fr.istic.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Version of an exam written in Typst (T25e, ADR 0005). Version 0 (A) is the subject as written: it holds the template,
 * the zones and the questions of the exam. The other versions only have their plan and their layout (JSON written by
 * the front, positions in 1/100000 of the page), used to rebuild their copies in the layout of version A. The PDF of a
 * version is in the object storage ({@code version/{examId}/{index}.pdf}), never in the database.
 */
@Entity
@Table(name = "exam_version", uniqueConstraints = @UniqueConstraint(name = "ux_exam_version_exam_idx", columnNames = { "exam_id", "idx" }))
@RegisterForReflection
public class ExamVersion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    public Exam exam;

    /** 0 to K - 1; copy c of a print run gets version (c - 1) mod K. */
    @Column(name = "idx", nullable = false)
    public int versionIndex;

    /** A to F. */
    @Column(name = "label", nullable = false, length = 2)
    public String label;

    /** Seed of the draw that gave the plans (the same seed gives the same versions). */
    @Column(name = "seed", nullable = false)
    public long seed;

    /** Plan given to the Typst package ({@code ce-plan}): order of sections and choices, variants. JSON. */
    @Lob
    @Column(name = "plan", nullable = false)
    public String plan;

    /** Layout of the version (format 1): zones of the questions, QCM boxes by canonical letter, identity zones. JSON. */
    @Lob
    @Column(name = "layout", nullable = false)
    public String layout;

    /** Pages of a copy, the same for all the versions. */
    @Column(name = "pages", nullable = false)
    public int pages;

    /** Size of the PDF in the storage, null while it is not uploaded. */
    @Column(name = "pdf_size")
    public Long pdfSize;

    @Column(name = "pdf_sha256", length = 64)
    public String pdfSha256;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "created_by", length = 50)
    public String createdBy;

    public static List<ExamVersion> findByExamId(long examId) {
        return find("exam.id = ?1 order by versionIndex", examId).list();
    }

    public static Optional<ExamVersion> findByExamIdAndIndex(long examId, int index) {
        return find("exam.id = ?1 and versionIndex = ?2", examId, index).firstResultOptional();
    }

    public static long deleteByExamId(long examId) {
        return delete("exam.id = ?1", examId);
    }
}
