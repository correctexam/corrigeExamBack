package fr.istic.domain;

import java.time.Instant;

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

/** One file of a Typst subject (T25c): stored under {@code subject/{examId}/files/{path}}. */
@Entity
@Table(name = "exam_subject_file", uniqueConstraints = @UniqueConstraint(name = "ux_exam_subject_file_path", columnNames = { "subject_id", "path" }))
@RegisterForReflection
public class ExamSubjectFile extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    public ExamSubject subject;

    /** Path relative to the subject root, validated by {@code SubjectPaths}. */
    @Column(name = "path", nullable = false, length = 200)
    public String path;

    @Column(name = "size", nullable = false)
    public long size;

    @Column(name = "sha256", nullable = false, length = 64)
    public String sha256;

    @Column(name = "content_type", nullable = false, length = 100)
    public String contentType;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
