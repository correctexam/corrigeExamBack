package fr.istic.service.customdto.correctexamstate;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
@RegisterForReflection
public class CorrectionExamState {
    private String nameExam;
    private List<QuestionState> questions = new ArrayList<QuestionState>();
    private List<StudentState> students = new ArrayList<StudentState>();

    public String getNameExam() { return nameExam; }
    public void setNameExam(String value) { this.nameExam = value; }

    public List<QuestionState> getQuestions() { return questions; }
    public void setQuestions(List<QuestionState> value) { this.questions = value; }

    public List<StudentState> getStudents() { return students; }
    public void setStudents(List<StudentState> value) { this.students = value; }

}
