package fr.istic.service.subject;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.ExamCopyVersion;
import fr.istic.service.dto.CopyVersionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Version of each copy of the scan of an exam (T25e6b, ADR 0005), for the statistics per version. The front writes
 * them after an alignment (local, or the bilan of the alignment worker); the whole list is replaced each time.
 */
@ApplicationScoped
public class CopyVersionService {

    private final Logger log = LoggerFactory.getLogger(CopyVersionService.class);

    Clock clock = Clock.systemUTC();

    @Transactional
    public List<CopyVersionDTO> list(long examId) {
        return ExamCopyVersion.findByExamId(examId).stream().map(CopyVersionService::toDto).toList();
    }

    /** Replaces the versions of the copies of the exam. */
    @Transactional
    public List<CopyVersionDTO> replace(long examId, List<CopyVersionDTO> copies) {
        VersionRules.checkCopyVersions(copies).ifPresent(reason -> {
            throw new VersionException(VersionException.Reason.INVALID, reason);
        });
        Exam exam = Exam.<Exam>findByIdOptional(examId)
                .orElseThrow(() -> new VersionException(VersionException.Reason.NOT_FOUND, "Exam " + examId + " not found"));
        ExamCopyVersion.deleteByExamId(examId);
        // the unique (exam, first page) must be free before the new rows are inserted
        ExamCopyVersion.flush();
        Instant now = clock.instant();
        for (CopyVersionDTO c : copies) {
            ExamCopyVersion row = new ExamCopyVersion();
            row.exam = exam;
            row.firstPage = c.firstPage();
            row.label = c.label();
            row.editionId = c.edition();
            row.copyNumber = c.copy();
            row.updatedAt = now;
            row.persist();
        }
        log.info("Exam {}: versions of {} copies recorded", examId, copies.size());
        return list(examId);
    }

    /** Version of each copy by the first page of its sheet in the scan, numbered from 0 ({@code ExamSheet.pagemin}). */
    @Transactional
    public Map<Integer, String> byPagemin(long examId) {
        return ExamCopyVersion.findByExamId(examId).stream()
                .collect(Collectors.toMap(c -> c.firstPage - 1, c -> c.label, (a, b) -> a));
    }

    /** Called when the exam is deleted, in the same transaction. */
    @Transactional
    public long deleteForExam(long examId) {
        return ExamCopyVersion.deleteByExamId(examId);
    }

    static CopyVersionDTO toDto(ExamCopyVersion c) {
        return new CopyVersionDTO(c.firstPage, c.label, c.editionId, c.copyNumber);
    }
}
