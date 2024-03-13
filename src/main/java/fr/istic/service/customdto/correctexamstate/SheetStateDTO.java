package fr.istic.service.customdto.correctexamstate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SheetStateDTO {
    private long id;
    private long answeredSheets;
    private long firstUnmarkedQuestion;

    public long getId() {
        return id;
    }

    public void setId(long value) {
        this.id = value;
    }


    public long getAnsweredSheets() {
        return answeredSheets;
    }

    public void setAnsweredSheets(long value) {
        this.answeredSheets = value;
    }

    public long getFirstUnmarkedQuestion() {
        return firstUnmarkedQuestion;
    }

    public void setFirstUnmarkedQuestion(long firstMarkedQuestion) {
        this.firstUnmarkedQuestion = firstMarkedQuestion;
    }
}
