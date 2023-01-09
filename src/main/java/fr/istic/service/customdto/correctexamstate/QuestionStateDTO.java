package fr.istic.service.customdto.correctexamstate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class QuestionStateDTO {
    private long id;
    private long answeredSheets;
    private long firstUnmarkedSheet;

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

    public long getFirstUnmarkedSheet() {
        return firstUnmarkedSheet;
    }

    public void setFirstUnmarkedSheet(long value) {
        this.firstUnmarkedSheet = value;
    }
}
