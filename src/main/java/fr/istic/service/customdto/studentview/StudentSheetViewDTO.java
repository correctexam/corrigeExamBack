package fr.istic.service.customdto.studentview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.istic.service.dto.QuestionDTO;
import fr.istic.service.dto.StudentResponseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Everything the student view of one exam sheet needs, in one response (ADR 0003, T10c): it replaces the anonymous
 * list requests filtered by sequential identifiers.
 */
@RegisterForReflection
@Schema(description = "One exam sheet as shown to its student: exam, questions, answers, comments and grade.")
public class StudentSheetViewDTO {

    @Schema(description = "UUID of the sheet (the capability of the /copie/{uuid} links).")
    public String uuid;

    @Schema(description = "Number of pages of the sheet; pages are numbered from 1 in /api/public/copie/{uuid}/page/{n}.")
    public int pages;

    @Schema(description = "Index of the sheet in the scan (0-based), as used by the cache of the exam.")
    public int sheetIndex;

    @Schema(description = "Exam of the sheet.")
    public ExamSummary exam = new ExamSummary();

    @Schema(description = "Students of the sheet (name and first name only).")
    public List<StudentIdentity> students = new ArrayList<>();

    @Schema(description = "True when answers, comments and grade are included (false: scanned sheet only).")
    public boolean corrections;

    @Schema(description = "Final grade of the exam, in points; null when not computed or not shown.")
    public Double finalNote;

    @Schema(description = "Questions of the exam, with their zone.")
    public List<QuestionDTO> questions = new ArrayList<>();

    @Schema(description = "Answers of the sheet with their text and graded comments (empty without corrections).")
    public List<StudentResponseDTO> responses = new ArrayList<>();

    @Schema(description = "Hybrid comments applied to each answer (step value > 0), by answer id.")
    public Map<Long, List<HybridCommentApplied>> hybridComments = new HashMap<>();

    @Schema(description = "Handwritten annotations of the graders (SVG), by zone: {numero}_{index of the zone in the question}.")
    public Map<String, String> annotations = new HashMap<>();

    @Schema(description = "E-mail addresses to contact about the grading: graders, else the teachers of the course.")
    public List<String> contactEmails = new ArrayList<>();

    @RegisterForReflection
    public static class ExamSummary {
        public Long id;
        public String name;
        public Long courseId;
        public String courseName;
    }

    @RegisterForReflection
    public static class StudentIdentity {
        public String name;
        public String firstname;

        public StudentIdentity() {
        }

        public StudentIdentity(String name, String firstname) {
            this.name = name;
            this.firstname = firstname;
        }
    }

    @RegisterForReflection
    public static class HybridCommentApplied {
        public Long id;
        public String text;
        public String description;
        public Double grade;
        public Boolean relative;
        public Integer step;
        public Integer stepValue;
    }
}
