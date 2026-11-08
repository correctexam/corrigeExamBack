package fr.istic.service.customdto.answernotebooks;

import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "The answer book of an exam sheet with its questions.")
public class AnswersNoteBook {
    @Schema(description = "Identifier of the related exam.")
    private Long examId;
    @Schema(description = "Name of the exam sheet.")
    private String sheetName;
    @Schema(description = "Questions of the answer book.")
    private List<QuestionNoteBook> questions;

    public Long getExamId() { return examId; }
    public void setExamId(Long value) { this.examId = value; }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String value) { this.sheetName = value; }

    public List<QuestionNoteBook> getQuestions() { return questions; }
    public void setQuestions(List<QuestionNoteBook> value) { this.questions = value; }
}
