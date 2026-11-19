package fr.istic.service.subject;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.ExamSubject;
import fr.istic.domain.ExamSubjectFile;
import fr.istic.service.FichierS3Service;
import fr.istic.service.StorageDeletionService;
import fr.istic.service.StorageException;
import fr.istic.service.StorageObject;
import fr.istic.service.dto.ExamSubjectDTO;
import fr.istic.service.dto.ExamSubjectFileDTO;
import fr.istic.service.dto.SubjectLockDTO;
import fr.istic.service.dto.SubjectSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Typst subjects of the exams (T25c, ADR 0004): files in the object storage, metadata and edition lock in the
 * database. The access to the exam is checked by the resource ({@code SecurityService.canAccess}).
 */
@ApplicationScoped
public class ExamSubjectService {

    /** Content of a file read from the storage. */
    public record FileContent(String path, String contentType, long size, InputStream stream) {
    }

    private final Logger log = LoggerFactory.getLogger(ExamSubjectService.class);

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    StorageDeletionService storageDeletionService;

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    @ConfigProperty(name = "correctexam.saveasfile", defaultValue = "false")
    boolean saveasfile;

    @ConfigProperty(name = "correctexam.subject.lock-ttl", defaultValue = "PT15M")
    Duration lockTtl;

    @ConfigProperty(name = "correctexam.subject.max-file-bytes", defaultValue = "5242880")
    long maxFileBytes;

    @ConfigProperty(name = "correctexam.subject.max-total-bytes", defaultValue = "52428800")
    long maxTotalBytes;

    @ConfigProperty(name = "correctexam.subject.max-files", defaultValue = "200")
    int maxFiles;

    Clock clock = Clock.systemUTC();

    /** Subjects need the object storage (S3 or local files); the database mode ({@code uses3=false}) has none. */
    public boolean isStorageAvailable() {
        return uses3 || saveasfile;
    }

    @Transactional
    public Optional<ExamSubjectDTO> find(long examId, String login) {
        return ExamSubject.findByExamId(examId).map(s -> toDto(s, login));
    }

    /** Creates the subject of the exam (idempotent); the creator holds the lock. */
    @Transactional
    public ExamSubjectDTO create(long examId, String login) {
        requireStorage();
        Optional<ExamSubject> existing = ExamSubject.findByExamId(examId);
        if (existing.isPresent()) {
            return toDto(existing.get(), login);
        }
        Exam exam = Exam.<Exam>findByIdOptional(examId)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.NOT_FOUND, "Exam " + examId + " not found"));
        Instant now = clock.instant();
        ExamSubject subject = new ExamSubject();
        subject.exam = exam;
        subject.revision = 0;
        subject.updatedAt = now;
        subject.updatedBy = login;
        subject.lockedBy = login;
        subject.lockExpiresAt = now.plus(lockTtl);
        subject.persist();
        return toDto(subject, login);
    }

    /** Takes or renews the lock; returns the subject (files and revision may have changed while someone else held it). */
    @Transactional
    public ExamSubjectDTO lock(long examId, String login, boolean force) {
        ExamSubject subject = subject(examId);
        Instant now = clock.instant();
        SubjectLocks.Decision d = SubjectLocks.acquire(lockOf(subject), login, force, now, lockTtl);
        if (d.outcome() == SubjectLocks.Outcome.HELD_BY_OTHER) {
            throw new SubjectException(SubjectException.Reason.LOCKED, "Subject locked by " + subject.lockedBy);
        }
        if (force && subject.lockedBy != null && !subject.lockedBy.equals(login) && lockOf(subject).isHeldAt(now)) {
            log.info("Subject of exam {}: lock of {} taken over by {}", examId, subject.lockedBy, login);
        }
        subject.lockedBy = d.lock().holder();
        subject.lockExpiresAt = d.lock().expiresAt();
        return toDto(subject, login);
    }

    @Transactional
    public void unlock(long examId, String login) {
        ExamSubject subject = subject(examId);
        SubjectLocks.Lock l = SubjectLocks.release(lockOf(subject), login, clock.instant());
        subject.lockedBy = l.holder();
        subject.lockExpiresAt = l.expiresAt();
    }

    /**
     * Writes a file. The caller must hold the lock; {@code expectedRevision}, when given, must be the current
     * revision (the editor sends the one it loaded). The object is stored before the commit: a failure of the commit
     * leaves an object under the exam prefix, removed with the exam or by the orphan cleanup.
     */
    @Transactional
    public ExamSubjectDTO writeFile(long examId, String rawPath, byte[] content, String login, Integer expectedRevision) {
        requireStorage();
        String path = SubjectPaths.normalize(rawPath)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.BAD_PATH, "Path not allowed: " + rawPath));
        if (content.length > maxFileBytes) {
            throw new SubjectException(SubjectException.Reason.TOO_LARGE, "File larger than " + maxFileBytes + " bytes");
        }
        ExamSubject subject = subject(examId);
        Instant now = clock.instant();
        checkWritable(subject, login, expectedRevision, now);

        Optional<ExamSubjectFile> existing = subject.file(path);
        long total = subject.files.stream().filter(f -> !f.path.equals(path)).mapToLong(f -> f.size).sum() + content.length;
        if (total > maxTotalBytes) {
            throw new SubjectException(SubjectException.Reason.TOO_LARGE, "Subject larger than " + maxTotalBytes + " bytes");
        }
        if (existing.isEmpty() && subject.files.size() >= maxFiles) {
            throw new SubjectException(SubjectException.Reason.TOO_LARGE, "More than " + maxFiles + " files");
        }

        String contentType = SubjectPaths.contentType(path);
        try {
            fichierS3Service.putObject(SubjectPaths.storageKey(examId, path), content, contentType);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IllegalStateException
                | StorageException e) {
            log.error("Subject of exam {}: could not store {}", examId, path, e);
            throw new SubjectException(SubjectException.Reason.STORAGE_UNAVAILABLE, "Could not store the file", e);
        }

        ExamSubjectFile file = existing.orElseGet(() -> {
            ExamSubjectFile f = new ExamSubjectFile();
            f.subject = subject;
            f.path = path;
            subject.files.add(f);
            return f;
        });
        file.size = content.length;
        file.sha256 = sha256(content);
        file.contentType = contentType;
        file.updatedAt = now;
        touch(subject, login, now);
        return toDto(subject, login);
    }

    @Transactional
    public ExamSubjectDTO deleteFile(long examId, String rawPath, String login, Integer expectedRevision) {
        String path = SubjectPaths.normalize(rawPath)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.BAD_PATH, "Path not allowed: " + rawPath));
        ExamSubject subject = subject(examId);
        Instant now = clock.instant();
        checkWritable(subject, login, expectedRevision, now);
        ExamSubjectFile file = subject.file(path)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.NOT_FOUND, "No file " + path));
        subject.files.remove(file);
        touch(subject, login, now);
        storageDeletionService.deleteAfterCommit(List.of(SubjectPaths.storageKey(examId, path)));
        return toDto(subject, login);
    }

    /** Opens a file of the subject; the caller closes the stream. */
    public FileContent readFile(long examId, String rawPath) {
        String path = SubjectPaths.normalize(rawPath)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.BAD_PATH, "Path not allowed: " + rawPath));
        ExamSubjectFileDTO file = fileMetadata(examId, path)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.NOT_FOUND, "No file " + path));
        try {
            return new FileContent(path, file.contentType(), file.size(),
                    fichierS3Service.getObject(SubjectPaths.storageKey(examId, path)));
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IllegalStateException e) {
            log.error("Subject of exam {}: could not read {}", examId, path, e);
            throw new SubjectException(SubjectException.Reason.STORAGE_UNAVAILABLE, "Could not read the file", e);
        }
    }

    /** Subjects of the exams of the courses the user teaches (all the subjects for an administrator). */
    @Transactional
    public List<SubjectSummaryDTO> listReusable(String login, boolean admin) {
        List<ExamSubject> subjects = admin
                ? ExamSubject.<ExamSubject>find("order by updatedAt desc").page(0, 500).list()
                : ExamSubject.<ExamSubject>find(
                        "select s from ExamSubject s join s.exam e join e.course c join c.profs u where u.login = ?1 order by s.updatedAt desc",
                        login).page(0, 500).list();
        return subjects.stream()
                .map(s -> new SubjectSummaryDTO(s.exam.id, s.exam.name, s.exam.course != null ? s.exam.course.id : null,
                        s.exam.course != null ? s.exam.course.name : null, s.updatedAt))
                .toList();
    }

    /**
     * Deletes the subject of an exam being deleted (inside the exam deletion transaction) and returns the storage
     * keys to delete after the commit: every object under {@code subject/{examId}/}.
     */
    @Transactional
    public List<String> prepareDeleteForExam(long examId) {
        Set<String> keys = new LinkedHashSet<>();
        ExamSubject.findByExamId(examId).ifPresent(s -> {
            s.files.forEach(f -> keys.add(SubjectPaths.storageKey(examId, f.path)));
            s.delete();
        });
        if (isStorageAvailable()) {
            try {
                for (StorageObject o : fichierS3Service.listObjects(SubjectPaths.subjectPrefix(examId))) {
                    keys.add(o.key());
                }
            } catch (StorageException e) {
                // the database is the reference: the listed files are deleted, the rest is left to the orphan cleanup
                log.warn("Subject of exam {}: could not list its storage objects ({})", examId, e.getMessage());
            }
        }
        return new ArrayList<>(keys);
    }

    private Optional<ExamSubjectFileDTO> fileMetadata(long examId, String path) {
        return ExamSubject.findByExamId(examId).flatMap(s -> s.file(path)).map(ExamSubjectService::fileDto);
    }

    private ExamSubject subject(long examId) {
        return ExamSubject.findByExamId(examId)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.NOT_FOUND, "Exam " + examId + " has no subject"));
    }

    private void checkWritable(ExamSubject subject, String login, Integer expectedRevision, Instant now) {
        if (!SubjectLocks.canWrite(lockOf(subject), login, now)) {
            throw new SubjectException(SubjectException.Reason.LOCKED,
                    subject.lockedBy != null && !subject.lockedBy.equals(login) && lockOf(subject).isHeldAt(now)
                            ? "Subject locked by " + subject.lockedBy
                            : "Take the lock before writing");
        }
        if (expectedRevision != null && expectedRevision != subject.revision) {
            throw new SubjectException(SubjectException.Reason.REVISION_CONFLICT,
                    "Subject at revision " + subject.revision + ", not " + expectedRevision);
        }
    }

    /** A change: new revision, author, and the lock of the writer is renewed. */
    private void touch(ExamSubject subject, String login, Instant now) {
        subject.revision++;
        subject.updatedAt = now;
        subject.updatedBy = login;
        subject.lockExpiresAt = now.plus(lockTtl);
    }

    private void requireStorage() {
        if (!isStorageAvailable()) {
            throw new SubjectException(SubjectException.Reason.STORAGE_UNAVAILABLE,
                    "Subjects need the object storage (correctexam.uses3=true or correctexam.saveasfile=true)");
        }
    }

    private static SubjectLocks.Lock lockOf(ExamSubject s) {
        return new SubjectLocks.Lock(s.lockedBy, s.lockExpiresAt);
    }

    private SubjectLockDTO lockDto(ExamSubject s, String login, Instant now) {
        SubjectLocks.Lock l = lockOf(s);
        boolean held = l.isHeldAt(now);
        return new SubjectLockDTO(held, held && l.holder().equals(login), held ? l.holder() : null, held ? l.expiresAt() : null);
    }

    private ExamSubjectDTO toDto(ExamSubject s, String login) {
        return new ExamSubjectDTO(s.exam.id, s.mainFile, s.revision, s.updatedAt, s.updatedBy,
                s.files.stream().map(ExamSubjectService::fileDto).toList(), lockDto(s, login, clock.instant()));
    }

    private static ExamSubjectFileDTO fileDto(ExamSubjectFile f) {
        return new ExamSubjectFileDTO(f.path, f.size, f.sha256, f.contentType, f.updatedAt);
    }

    static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
