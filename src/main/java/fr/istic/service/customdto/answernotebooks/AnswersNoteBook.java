package fr.istic.service.customdto.answernotebooks;

import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;
@RegisterForReflection
public class AnswersNoteBook {
    private Long examId;
    private String sheetName;
    private List<QuestionNoteBook> questions;

    public Long getExamId() { return examId; }
    public void setExamId(Long value) { this.examId = value; }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String value) { this.sheetName = value; }

    public List<QuestionNoteBook> getQuestions() { return questions; }
    public void setQuestions(List<QuestionNoteBook> value) { this.questions = value; }
}
