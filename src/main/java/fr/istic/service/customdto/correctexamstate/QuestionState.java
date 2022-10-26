package fr.istic.service.customdto.correctexamstate;

import io.quarkus.runtime.annotations.RegisterForReflection;
@RegisterForReflection
public class QuestionState {
    private Long id;
    private Long answeredSheets;
    private Long firstUnmarkedSheet;

    public Long getID() { return id; }
    public void setID(Long value) { this.id = value; }

    public Long getAnsweredSheets() { return answeredSheets; }
    public void setAnsweredSheets(Long value) { this.answeredSheets = value; }

    public Long getFirstUnmarkedSheet() { return firstUnmarkedSheet; }
    public void setFirstUnmarkedSheet(Long value) { this.firstUnmarkedSheet = value; }

}
