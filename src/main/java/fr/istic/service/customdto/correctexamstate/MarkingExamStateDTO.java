package fr.istic.service.customdto.correctexamstate;

import java.util.ArrayList;

import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MarkingExamStateDTO {
    private String nameExam;
    private List<QuestionStateDTO> questions = new ArrayList<QuestionStateDTO>();
    private List<SheetStateDTO> sheets = new ArrayList<SheetStateDTO>();

    public String getNameExam() { return nameExam; }
    public void setNameExam(String value) { this.nameExam = value; }

    public List<QuestionStateDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionStateDTO> value) { this.questions = value; }

    public List<SheetStateDTO> getSheets() { return sheets; }
    public void setSheets(List<SheetStateDTO> value) { this.sheets = value; }

}
