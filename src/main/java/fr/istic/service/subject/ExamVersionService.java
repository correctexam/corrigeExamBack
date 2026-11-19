package fr.istic.service.subject;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.istic.domain.Exam;
import fr.istic.domain.ExamEdition;
import fr.istic.domain.ExamVersion;
import fr.istic.service.FichierS3Service;
import fr.istic.service.StorageDeletionService;
import fr.istic.service.StorageException;
import fr.istic.service.StorageObject;
import fr.istic.service.customdto.ScanRequestDto;
import fr.istic.service.dto.ExamVersionDTO;
import fr.istic.service.dto.ExamVersionsRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Versions of the exams written in Typst (T25e, ADR 0005): plan and layout in the database, PDF in the object storage
 * ({@code version/{examId}/{index}.pdf}). The versions are frozen as soon as a print run of several versions exists.
 * The access to the exam is checked by the resource ({@code SecurityService.canAccess}).
 */
@ApplicationScoped
public class ExamVersionService {

    /** PDF of a version read from the storage. */
    public record PdfContent(long size, InputStream stream) {
    }

    private final Logger log = LoggerFactory.getLogger(ExamVersionService.class);

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    StorageDeletionService storageDeletionService;

    @Inject
    ExamSubjectService examSubjectService;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "correctexam.version.max-pdf-bytes", defaultValue = "52428800")
    long maxPdfBytes;

    Clock clock = Clock.systemUTC();

    @Transactional
    public List<ExamVersionDTO> list(long examId) {
        return ExamVersion.findByExamId(examId).stream().map(this::toDto).toList();
    }

    /** True when the versions can no longer change (a print run of several versions exists). */
    @Transactional
    public boolean isFrozen(long examId) {
        return ExamEdition.hasSeveralVersions(examId);
    }

    /**
     * Replaces the versions of the exam. The PDFs of the previous versions are deleted after the commit: the front
     * sends the PDF of each new version afterwards.
     */
    @Transactional
    public List<ExamVersionDTO> replace(long examId, ExamVersionsRequestDTO request, String login) {
        requireStorage();
        VersionRules.check(request).ifPresent(reason -> {
            throw new VersionException(VersionException.Reason.INVALID, reason);
        });
        Exam exam = Exam.<Exam>findByIdOptional(examId)
                .orElseThrow(() -> new VersionException(VersionException.Reason.NOT_FOUND, "Exam " + examId + " not found"));
        requireNotFrozen(examId);

        List<String> oldKeys = deleteRows(examId);
        Instant now = clock.instant();
        List<ExamVersionDTO> created = new ArrayList<>();
        for (ExamVersionsRequestDTO.Version v : request.versions()) {
            ExamVersion version = new ExamVersion();
            version.exam = exam;
            version.versionIndex = v.index();
            version.label = VersionRules.label(v.index());
            version.seed = request.seed();
            version.plan = v.plan().toString();
            version.layout = v.layout().toString();
            version.pages = v.pages();
            version.createdAt = now;
            version.createdBy = login;
            version.persist();
            created.add(toDto(version));
        }
        storageDeletionService.deleteAfterCommit(oldKeys);
        log.info("Exam {}: {} version(s) recorded by {} (seed {})", examId, created.size(), login, request.seed());
        return created;
    }

    /** Deletes the versions of the exam (back to a single subject); their PDFs after the commit. */
    @Transactional
    public void clear(long examId, String login) {
        requireNotFrozen(examId);
        List<String> keys = deleteRows(examId);
        storageDeletionService.deleteAfterCommit(keys);
        log.info("Exam {}: versions deleted by {}", examId, login);
    }

    /** Stores the PDF of a version (replaces it). */
    @Transactional
    public ExamVersionDTO writePdf(long examId, int index, byte[] content, String login) {
        requireStorage();
        if (content == null || content.length > maxPdfBytes) {
            throw new VersionException(content == null ? VersionException.Reason.INVALID : VersionException.Reason.TOO_LARGE,
                    content == null ? "No PDF" : "PDF larger than " + maxPdfBytes + " bytes");
        }
        if (!VersionRules.isPdf(content)) {
            throw new VersionException(VersionException.Reason.INVALID, "Not a PDF");
        }
        ExamVersion version = version(examId, index);
        requireNotFrozen(examId);
        try {
            fichierS3Service.putObject(VersionRules.storageKey(examId, index), content, "application/pdf");
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IllegalStateException
                | StorageException e) {
            log.error("Exam {}: could not store the PDF of version {}", examId, version.label, e);
            throw new VersionException(VersionException.Reason.STORAGE_UNAVAILABLE, "Could not store the PDF", e);
        }
        version.pdfSize = (long) content.length;
        version.pdfSha256 = sha256(content);
        log.info("Exam {}: PDF of version {} stored by {} ({} bytes)", examId, version.label, login, content.length);
        return toDto(version);
    }

    /** Opens the PDF of a version; the caller closes the stream. */
    public PdfContent readPdf(long examId, int index) {
        long size = pdfSize(examId, index);
        try {
            return new PdfContent(size, fichierS3Service.getObject(VersionRules.storageKey(examId, index)));
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IllegalStateException e) {
            log.error("Exam {}: could not read the PDF of version {}", examId, index, e);
            throw new VersionException(VersionException.Reason.STORAGE_UNAVAILABLE, "Could not read the PDF", e);
        }
    }

    /**
     * Layouts of the versions sent to the alignment worker (T25e5): all the versions when a print run of several
     * versions exists, none otherwise (nothing to rebuild).
     */
    @Transactional
    public List<ScanRequestDto.VersionLayout> layoutsForWorker(long examId) {
        if (!ExamEdition.hasSeveralVersions(examId)) {
            return List.of();
        }
        return ExamVersion.findByExamId(examId).stream()
                .map(v -> new ScanRequestDto.VersionLayout(v.versionIndex, v.label, json(v.layout)))
                .toList();
    }

    /** Checks a print run of {@code versions} versions against the stored versions; empty when it is valid. */
    @Transactional
    public Optional<String> checkEdition(long examId, Integer versions, int pagesPerCopy) {
        List<ExamVersion> stored = ExamVersion.findByExamId(examId);
        Set<Integer> withPdf = stored.stream().filter(v -> v.pdfSize != null).map(v -> v.versionIndex).collect(Collectors.toSet());
        Integer pages = stored.isEmpty() ? null : stored.get(0).pages;
        return VersionRules.checkEdition(versions, pagesPerCopy, pages, withPdf);
    }

    /**
     * Deletes the versions of an exam being deleted (inside the exam deletion transaction) and returns the storage
     * keys to delete after the commit: every object under {@code version/{examId}/}.
     */
    @Transactional
    public List<String> prepareDeleteForExam(long examId) {
        Set<String> keys = new LinkedHashSet<>(deleteRows(examId));
        if (examSubjectService.isStorageAvailable()) {
            try {
                for (StorageObject o : fichierS3Service.listObjects(VersionRules.examPrefix(examId))) {
                    keys.add(o.key());
                }
            } catch (StorageException e) {
                // the database is the reference: the known PDFs are deleted, the rest is left to the orphan cleanup
                log.warn("Exam {}: could not list the PDFs of its versions ({})", examId, e.getMessage());
            }
        }
        return new ArrayList<>(keys);
    }

    private List<String> deleteRows(long examId) {
        List<String> keys = ExamVersion.findByExamId(examId).stream()
                .filter(v -> v.pdfSize != null)
                .map(v -> VersionRules.storageKey(examId, v.versionIndex))
                .toList();
        ExamVersion.deleteByExamId(examId);
        // the unique (exam, index) must be free before the new rows are inserted
        ExamVersion.flush();
        return keys;
    }

    @Transactional
    long pdfSize(long examId, int index) {
        ExamVersion version = version(examId, index);
        if (version.pdfSize == null) {
            throw new VersionException(VersionException.Reason.NOT_FOUND, "Version " + version.label + " has no PDF");
        }
        return version.pdfSize;
    }

    private ExamVersion version(long examId, int index) {
        return ExamVersion.findByExamIdAndIndex(examId, index)
                .orElseThrow(() -> new VersionException(VersionException.Reason.NOT_FOUND, "Exam " + examId + " has no version " + index));
    }

    private void requireNotFrozen(long examId) {
        if (ExamEdition.hasSeveralVersions(examId)) {
            throw new VersionException(VersionException.Reason.FROZEN, "A print run of several versions exists: the versions can no longer change");
        }
    }

    private void requireStorage() {
        if (!examSubjectService.isStorageAvailable()) {
            throw new VersionException(VersionException.Reason.STORAGE_UNAVAILABLE, "No object storage (correctexam.uses3 / saveasfile)");
        }
    }

    ExamVersionDTO toDto(ExamVersion v) {
        return new ExamVersionDTO(v.id, v.exam.id, v.versionIndex, v.label, v.seed, json(v.plan), json(v.layout), v.pages, v.pdfSize != null,
                v.pdfSize, v.createdAt, v.createdBy);
    }

    private JsonNode json(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (JsonProcessingException e) {
            // written by this service from parsed JSON: cannot happen unless the row was edited by hand
            throw new IllegalStateException("Stored JSON of a version is not valid", e);
        }
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
