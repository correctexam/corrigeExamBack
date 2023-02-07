package fr.istic.service.customdto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.istic.service.dto.CommentsDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Answer4QuestionDTO {

    String studentName;

    Double note;
    boolean star = false;
    boolean worststar = false;
    int pagemin = 0;
    int pagemax = 0;
    List<CommentsDTO> comments = new ArrayList<>();
    List<Long> textComments = new ArrayList<>();
    List<Long> gradedComments = new ArrayList<>();


    public Answer4QuestionDTO() {
    }

    public Answer4QuestionDTO(String studentName, Double note, boolean star, boolean worststar, int pagemin, int pagemax, List<CommentsDTO> comments, List<Long> textComments, List<Long> gradedComments) {
        this.studentName = studentName;
        this.note = note;
        this.star = star;
        this.worststar = worststar;
        this.pagemin = pagemin;
        this.pagemax = pagemax;
        this.comments = comments;
        this.textComments = textComments;
        this.gradedComments = gradedComments;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Double getNote() {
        return this.note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public boolean isStar() {
        return this.star;
    }

    public boolean getStar() {
        return this.star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public boolean isWorststar() {
        return this.worststar;
    }

    public boolean getWorststar() {
        return this.worststar;
    }

    public void setWorststar(boolean worststar) {
        this.worststar = worststar;
    }

    public int getPagemin() {
        return this.pagemin;
    }

    public void setPagemin(int pagemin) {
        this.pagemin = pagemin;
    }

    public int getPagemax() {
        return this.pagemax;
    }

    public void setPagemax(int pagemax) {
        this.pagemax = pagemax;
    }

    public List<CommentsDTO> getComments() {
        return this.comments;
    }

    public void setComments(List<CommentsDTO> comments) {
        this.comments = comments;
    }

    public List<Long> getTextComments() {
        return this.textComments;
    }

    public void setTextComments(List<Long> textComments) {
        this.textComments = textComments;
    }

    public List<Long> getGradedComments() {
        return this.gradedComments;
    }

    public void setGradedComments(List<Long> gradedComments) {
        this.gradedComments = gradedComments;
    }

    public Answer4QuestionDTO studentName(String studentName) {
        setStudentName(studentName);
        return this;
    }

    public Answer4QuestionDTO note(Double note) {
        setNote(note);
        return this;
    }

    public Answer4QuestionDTO star(boolean star) {
        setStar(star);
        return this;
    }

    public Answer4QuestionDTO worststar(boolean worststar) {
        setWorststar(worststar);
        return this;
    }

    public Answer4QuestionDTO pagemin(int pagemin) {
        setPagemin(pagemin);
        return this;
    }

    public Answer4QuestionDTO pagemax(int pagemax) {
        setPagemax(pagemax);
        return this;
    }

    public Answer4QuestionDTO comments(List<CommentsDTO> comments) {
        setComments(comments);
        return this;
    }

    public Answer4QuestionDTO textComments(List<Long> textComments) {
        setTextComments(textComments);
        return this;
    }

    public Answer4QuestionDTO gradedComments(List<Long> gradedComments) {
        setGradedComments(gradedComments);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Answer4QuestionDTO)) {
            return false;
        }
        Answer4QuestionDTO answer4QuestionDTO = (Answer4QuestionDTO) o;
        return Objects.equals(studentName, answer4QuestionDTO.studentName) && Objects.equals(note, answer4QuestionDTO.note) && star == answer4QuestionDTO.star && worststar == answer4QuestionDTO.worststar && pagemin == answer4QuestionDTO.pagemin && pagemax == answer4QuestionDTO.pagemax && Objects.equals(comments, answer4QuestionDTO.comments) && Objects.equals(textComments, answer4QuestionDTO.textComments) && Objects.equals(gradedComments, answer4QuestionDTO.gradedComments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentName, note, star, worststar, pagemin, pagemax, comments, textComments, gradedComments);
    }

    @Override
    public String toString() {
        return "{" +
            " studentName='" + getStudentName() + "'" +
            ", note='" + getNote() + "'" +
            ", star='" + isStar() + "'" +
            ", worststar='" + isWorststar() + "'" +
            ", pagemin='" + getPagemin() + "'" +
            ", pagemax='" + getPagemax() + "'" +
            ", comments='" + getComments() + "'" +
            ", textComments='" + getTextComments() + "'" +
            ", gradedComments='" + getGradedComments() + "'" +
            "}";
    }

}
