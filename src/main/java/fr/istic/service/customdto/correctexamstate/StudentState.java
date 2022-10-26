package fr.istic.service.customdto.correctexamstate;

import io.quarkus.runtime.annotations.RegisterForReflection;
@RegisterForReflection
public class StudentState {
    private Long id;
    private Long answeredSheets;
    private Long firstUnmarkedQuestion;

    public Long getID() { return id; }
    public void setID(Long value) { this.id = value; }

    public Long getAnsweredSheets() { return answeredSheets; }
    public void setAnsweredSheets(Long value) { this.answeredSheets = value; }
    public Long getFirstUnmarkedQuestion() {
        return firstUnmarkedQuestion;
    }
    public void setFirstUnmarkedQuestion(Long firstUnmarkedQuestion) {
        this.firstUnmarkedQuestion = firstUnmarkedQuestion;
    }
}
