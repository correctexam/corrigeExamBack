package fr.istic.service.subject;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.ExamEdition;
import fr.istic.service.customdto.ScanRequestDto;
import fr.istic.service.dto.ExamEditionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** Print runs of the exams written in Typst (T25d, ADR 0004): only metadata, the PDF is made by the front. */
@ApplicationScoped
public class ExamEditionService {

    private final Logger log = LoggerFactory.getLogger(ExamEditionService.class);

    Clock clock = Clock.systemUTC();

    /** Records a print run; empty when the exam does not exist. */
    @Transactional
    public Optional<ExamEditionDTO> create(long examId, int copies, int pagesPerCopy, int versions, String login) {
        Optional<Exam> exam = Exam.findByIdOptional(examId);
        if (exam.isEmpty()) {
            return Optional.empty();
        }
        ExamEdition edition = new ExamEdition();
        edition.exam = exam.get();
        edition.copies = copies;
        edition.pagesPerCopy = pagesPerCopy;
        edition.versions = versions;
        edition.createdAt = Instant.now(clock);
        edition.createdBy = login;
        edition.persist();
        log.info("Exam {}: print run {} of {} copies x {} pages, {} version(s), by {}", examId, edition.id, copies, pagesPerCopy, versions, login);
        return Optional.of(toDto(edition));
    }

    @Transactional
    public List<ExamEditionDTO> list(long examId) {
        return ExamEdition.findByExamId(examId).stream().map(ExamEditionService::toDto).toList();
    }

    /** Print runs in the form sent to the alignment worker (T25d3); empty when the exam was never printed. */
    @Transactional
    public List<ScanRequestDto.PageCodeEdition> pageCodes(long examId) {
        return ExamEdition.findByExamId(examId).stream()
                .map(e -> new ScanRequestDto.PageCodeEdition(e.id, e.copies, e.pagesPerCopy, e.versions))
                .toList();
    }

    /** Called when the exam is deleted, in the same transaction. */
    @Transactional
    public long deleteForExam(long examId) {
        return ExamEdition.deleteByExamId(examId);
    }

    static ExamEditionDTO toDto(ExamEdition e) {
        return new ExamEditionDTO(e.id, e.exam.id, e.copies, e.pagesPerCopy, e.versions, e.createdAt, e.createdBy);
    }
}
