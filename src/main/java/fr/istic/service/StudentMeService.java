package fr.istic.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.Student;
import fr.istic.domain.enumeration.StudentAccess;
import fr.istic.service.customdto.studentportal.StudentPersonalDataDTO;
import fr.istic.service.customdto.studentportal.StudentSheetSummaryDTO;
import fr.istic.service.customdto.studentview.StudentSheetViewDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * What the student of a portal session can read (ADR 0003): the sheets of the student records that carry the e-mail
 * address of the session, filtered by the student access of their exam. A sheet is reached by its identifier, never by
 * its UUID: the UUID opens the corrected sheet whatever the publication of the exam.
 */
@ApplicationScoped
public class StudentMeService {

    @Inject
    StudentSheetViewService studentSheetViewService;

    /** A sheet of the student with its exam, when the exam is not hidden. */
    public record OwnedSheet(ExamSheet sheet, Exam exam) {
        public StudentAccess access() {
            return exam.studentAccess != null ? exam.studentAccess : StudentAccess.SHEET;
        }

        public boolean graded() {
            return access() == StudentAccess.GRADED;
        }
    }

    private static List<Student> studentsOf(String email) {
        return Student.<Student>list("lower(trim(mail)) = ?1", StudentPortalService.normalizeEmail(email));
    }

    /** Sheets of the student whose exam is not hidden, most recent exam first. */
    @Transactional
    public List<OwnedSheet> ownedSheets(String email) {
        return ownedSheets(email, false);
    }

    private List<OwnedSheet> ownedSheets(String email, boolean includeHidden) {
        List<OwnedSheet> owned = new ArrayList<>();
        for (Student student : studentsOf(email)) {
            for (ExamSheet sheet : student.examSheets) {
                if (sheet.scan == null || sheet.pagemin == null || sheet.pagemin < 0 || sheet.pagemax == null) {
                    continue;
                }
                Exam.findExambyScanId(sheet.scan.id).firstResultOptional()
                        .filter(e -> includeHidden || e.studentAccess != StudentAccess.HIDDEN)
                        .filter(e -> owned.stream().noneMatch(o -> o.sheet().id.equals(sheet.id)))
                        .ifPresent(e -> owned.add(new OwnedSheet(sheet, e)));
            }
        }
        owned.sort(Comparator.comparing((OwnedSheet o) -> o.exam().id).reversed());
        return owned;
    }

    @Transactional
    public List<StudentSheetSummaryDTO> sheets(String email) {
        List<Student> students = studentsOf(email);
        return ownedSheets(email).stream().map(o -> summary(o, students)).toList();
    }

    private StudentSheetSummaryDTO summary(OwnedSheet o, List<Student> students) {
        StudentSheetSummaryDTO dto = new StudentSheetSummaryDTO();
        dto.sheetId = o.sheet().id;
        dto.examId = o.exam().id;
        dto.examName = o.exam().name;
        if (o.exam().course != null) {
            dto.courseId = o.exam().course.id;
            dto.courseName = o.exam().course.name;
        }
        dto.access = o.access();
        if (o.graded()) {
            students.stream()
                    .filter(s -> s.examSheets.stream().anyMatch(sh -> sh.id.equals(o.sheet().id)))
                    .findFirst()
                    .flatMap(s -> FinalResult.findFinalResultByStudentIdAndExamId(s.id, o.exam().id).firstResultOptional())
                    .filter(r -> r.note != null)
                    .ifPresent(r -> dto.finalNote = r.note / 100.0);
        }
        return dto;
    }

    /** A sheet of the student, or empty when it is not theirs or its exam is hidden. */
    @Transactional
    public Optional<OwnedSheet> ownedSheet(String email, long sheetId) {
        return ownedSheets(email).stream().filter(o -> o.sheet().id == sheetId).findFirst();
    }

    /** View of a sheet of the student: corrections only when the exam is GRADED; never the UUID of the sheet. */
    @Transactional
    public Optional<StudentSheetViewDTO> view(String email, long sheetId) {
        return ownedSheet(email, sheetId)
                .flatMap(o -> studentSheetViewService.view(o.sheet(), o.graded()))
                .map(dto -> {
                    dto.uuid = null;
                    return dto;
                });
    }

    public Optional<String> page(String email, long sheetId, int page, boolean noalign) throws IOException {
        Optional<OwnedSheet> o = ownedSheet(email, sheetId);
        if (o.isEmpty()) {
            return Optional.empty();
        }
        return studentSheetViewService.page(o.get().sheet(), o.get().exam().id, page, noalign);
    }

    /** Number of starred answers to a question, for a GRADED exam of the student only. */
    public Optional<Integer> countBestAnswers(String email, long sheetId, int numero) {
        return ownedSheet(email, sheetId).filter(OwnedSheet::graded)
                .map(o -> studentSheetViewService.bestAnswerSheets(o.exam().id, numero).size());
    }

    public Optional<String> bestAnswerPage(String email, long sheetId, int numero, int index, int page, boolean noalign) throws IOException {
        Optional<OwnedSheet> o = ownedSheet(email, sheetId).filter(OwnedSheet::graded);
        if (o.isEmpty()) {
            return Optional.empty();
        }
        return studentSheetViewService.bestAnswerPage(o.get().exam().id, numero, index, page, noalign);
    }

    /** Personal data held on the student (GDPR article 15): every record with their address and their sheets. */
    @Transactional
    public StudentPersonalDataDTO personalData(String email) {
        StudentPersonalDataDTO dto = new StudentPersonalDataDTO();
        dto.email = StudentPortalService.normalizeEmail(email);
        List<Student> students = studentsOf(email);
        for (Student s : students) {
            StudentPersonalDataDTO.StudentRecord r = new StudentPersonalDataDTO.StudentRecord();
            r.name = s.name;
            r.firstname = s.firstname;
            r.ine = s.ine;
            r.mail = s.mail;
            r.caslogin = s.caslogin;
            s.groups.forEach(g -> {
                if (g.course != null && !r.courses.contains(g.course.name)) {
                    r.courses.add(g.course.name);
                }
                r.groups.add(g.groupName);
            });
            dto.records.add(r);
        }
        // Every sheet held on the student, hidden exams included (without grade unless GRADED)
        dto.sheets = ownedSheets(email, true).stream().map(o -> summary(o, students)).toList();
        return dto;
    }
}
