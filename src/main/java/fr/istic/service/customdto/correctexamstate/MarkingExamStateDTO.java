package fr.istic.service.customdto.correctexamstate;

import java.util.ArrayList;

import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "State of an exam marking session.")
public class MarkingExamStateDTO {
    @Schema(description = "Name of the exam.")
    private String nameExam;
    @Schema(description = "State of each question.")
    private List<QuestionStateDTO> questions = new ArrayList<QuestionStateDTO>();
    @Schema(description = "State of each sheet.")
    private List<SheetStateDTO> sheets = new ArrayList<SheetStateDTO>();

    public String getNameExam() { return nameExam; }
    public void setNameExam(String value) { this.nameExam = value; }

    public List<QuestionStateDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionStateDTO> value) { this.questions = value; }

    public List<SheetStateDTO> getSheets() { return sheets; }
    public void setSheets(List<SheetStateDTO> value) { this.sheets = value; }

}
