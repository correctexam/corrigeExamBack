package fr.istic.service.customdto.correctexamstate;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Marking state of a sheet.")
public class SheetStateDTO {
    @Schema(description = "Identifier of the sheet.")
    private long id;
    @Schema(description = "Number of answered questions.")
    private long answeredSheets;
    @Schema(description = "Index of the first unmarked question.")
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
