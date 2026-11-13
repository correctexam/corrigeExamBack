package fr.istic.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.Question;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.service.customdto.studentview.StudentSheetViewDTO;
import fr.istic.service.customdto.studentview.StudentSheetViewDTO.HybridCommentApplied;
import fr.istic.service.customdto.studentview.StudentSheetViewDTO.StudentIdentity;
import fr.istic.service.dto.HybridGradedCommentDTO;
import fr.istic.service.mapper.HybridGradedCommentMapper;
import fr.istic.service.mapper.QuestionMapper;
import fr.istic.service.mapper.StudentResponseMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Data of one exam sheet for its student (ADR 0003, T10c). Everything is reached from the sheet itself (its UUID,
 * or later the session of the student): no list of another exam, sheet or student can be read through it.
 */
@ApplicationScoped
public class StudentSheetViewService {

    private static final Logger log = LoggerFactory.getLogger(StudentSheetViewService.class);

    @Inject
    QuestionMapper questionMapper;

    @Inject
    StudentResponseMapper studentResponseMapper;

    @Inject
    HybridGradedCommentMapper hybridGradedCommentMapper;

    @Inject
    CourseService courseService;

    @Inject
    CacheUploadService cacheUploadService;

    /** Sheet with this UUID, when it belongs to an exam and to scanned pages (not an orphan sheet). */
    @Transactional
    public Optional<ExamSheet> findSheet(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return Optional.empty();
        }
        return ExamSheet.findExamSheetByName(uuid).firstResultOptional()
                .filter(s -> s.scan != null && s.pagemin != null && s.pagemax != null && s.pagemin >= 0 && s.pagemax >= s.pagemin);
    }

    /** Exam whose scan holds the sheet. */
    @Transactional
    public Optional<Exam> examOf(ExamSheet sheet) {
        return Exam.findExambyScanId(sheet.scan.id).firstResultOptional();
    }

    /**
     * View of the sheet. Without corrections (exam published as SHEET in the student portal), only the exam, the
     * questions and their zones are returned: no answer, comment, grade or grader.
     */
    @Transactional
    public Optional<StudentSheetViewDTO> view(String uuid, boolean withCorrections) {
        Optional<ExamSheet> found = findSheet(uuid);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ExamSheet sheet = found.get();
        Optional<Exam> exam = examOf(sheet);
        if (exam.isEmpty()) {
            return Optional.empty();
        }
        Exam ex = exam.get();

        StudentSheetViewDTO dto = new StudentSheetViewDTO();
        dto.uuid = sheet.name;
        dto.pages = sheet.pagemax - sheet.pagemin + 1;
        dto.sheetIndex = sheet.pagemin / dto.pages;
        dto.exam.id = ex.id;
        dto.exam.name = ex.name;
        if (ex.course != null) {
            dto.exam.courseId = ex.course.id;
            dto.exam.courseName = ex.course.name;
        }
        List<Student> students = sheet.students.stream().sorted(Comparator.comparing(s -> s.id)).toList();
        students.forEach(s -> dto.students.add(new StudentIdentity(s.name, s.firstname)));
        dto.questions = Question.findQuestionbyExamId(ex.id).list().stream()
                .sorted(Comparator.comparing((Question q) -> q.numero).thenComparing(q -> q.id))
                .map(questionMapper::toDto)
                .toList();

        dto.corrections = withCorrections;
        if (!withCorrections) {
            return Optional.of(dto);
        }

        if (!students.isEmpty()) {
            FinalResult.findFinalResultByStudentIdAndExamId(students.get(0).id, ex.id).firstResultOptional()
                    .filter(r -> r.note != null)
                    .ifPresent(r -> dto.finalNote = r.note / 100.0);
        }

        List<StudentResponse> responses = StudentResponse.findStudentResponsesbysheetId(sheet.id).list();
        Set<String> graders = new LinkedHashSet<>();
        for (StudentResponse response : responses) {
            dto.responses.add(studentResponseMapper.toDto(response));
            if (response.correctedBy != null && response.correctedBy.email != null && !response.correctedBy.email.isBlank()) {
                graders.add(response.correctedBy.email);
            }
            if (response.question != null && response.question.gradeType == GradeType.HYBRID) {
                List<HybridCommentApplied> applied = Answer2HybridGradedComment
                        .findAllAnswerHybridGradedCommentByAnswerId(response.id).list().stream()
                        .filter(a -> a.stepValue != null && a.stepValue > 0 && a.hybridcomments != null)
                        .map(a -> applied(a))
                        .collect(Collectors.toList());
                if (!applied.isEmpty()) {
                    dto.hybridComments.put(response.id, applied);
                }
            }
        }
        if (!graders.isEmpty()) {
            dto.contactEmails.addAll(graders);
        } else if (ex.course != null) {
            dto.contactEmails.addAll(courseService.getAllProfMail(ex.course.id).getEmails().stream()
                    .filter(e -> e != null && !e.isBlank()).toList());
        }
        return Optional.of(dto);
    }

    private HybridCommentApplied applied(Answer2HybridGradedComment a) {
        HybridGradedCommentDTO c = hybridGradedCommentMapper.toDto(a.hybridcomments);
        HybridCommentApplied h = new HybridCommentApplied();
        h.id = c.id;
        h.text = c.text;
        h.description = c.description;
        h.grade = c.grade;
        h.relative = c.relative;
        h.step = c.step;
        h.stepValue = a.stepValue;
        return h;
    }

    /**
     * Page {@code page} (1 to the number of pages) of the sheet, as stored in the cache of the exam.
     *
     * @return empty when the page is not a page of the sheet
     * @throws IOException when the cache cannot be read
     */
    public Optional<String> page(ExamSheet sheet, long examId, int page, boolean noalign) throws IOException {
        int pages = sheet.pagemax - sheet.pagemin + 1;
        if (page < 1 || page > pages) {
            return Optional.empty();
        }
        // The cache numbers the pages of the scan from 1
        return Optional.of(cacheUploadService.getAlignPage(examId, sheet.pagemin + page, noalign));
    }

    /** Sheets whose answer to question {@code numero} is starred by the graders, in a stable order. */
    @Transactional
    public List<ExamSheet> bestAnswerSheets(long examId, int numero) {
        return StudentResponse.getBestAnswerforQuestionNoAndExamId(examId, numero).list().stream()
                .sorted(Comparator.comparing(s -> s.id))
                .toList();
    }

    /** Pages (from 1) of the sheets that hold a zone of question {@code numero}. */
    @Transactional
    public Set<Integer> pagesOfQuestion(long examId, int numero) {
        return Question.findQuestionbyExamIdandnumero(examId, numero).list().stream()
                .filter(q -> q.zone != null && q.zone.pageNumber != null)
                .map(q -> q.zone.pageNumber)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Page {@code page} of the {@code index}-th starred answer to question {@code numero}, without anything that
     * identifies its student; only the pages that hold the question can be read.
     */
    public Optional<String> bestAnswerPage(long examId, int numero, int index, int page, boolean noalign) throws IOException {
        List<ExamSheet> sheets = bestAnswerSheets(examId, numero);
        if (index < 0 || index >= sheets.size() || !pagesOfQuestion(examId, numero).contains(page)) {
            log.debug("No starred answer page {} / {} / {} for exam {}", numero, index, page, examId);
            return Optional.empty();
        }
        return page(sheets.get(index), examId, page, noalign);
    }
}
