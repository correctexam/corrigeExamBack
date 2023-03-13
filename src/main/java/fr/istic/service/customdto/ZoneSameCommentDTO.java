package fr.istic.service.customdto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.istic.domain.enumeration.GradeType;
import fr.istic.service.dto.GradedCommentDTO;
import fr.istic.service.dto.TextCommentDTO;
import fr.istic.service.dto.ZoneDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ZoneSameCommentDTO {

    List<ZoneDTO> zones;
    List<Answer4QuestionDTO> answers;
    List<TextCommentDTO> textComments = new ArrayList<>();
    List<GradedCommentDTO> gradedComments = new ArrayList<>();

     Double point;
     Integer step;
     String validExpression;
     GradeType gradeType;
     Integer numero;
     String algoName;



    public ZoneSameCommentDTO() {
    }

    public ZoneSameCommentDTO(List<ZoneDTO> zones, List<Answer4QuestionDTO> answers, List<TextCommentDTO> textComments, List<GradedCommentDTO> gradedComments, Double point, Integer step, String validExpression, GradeType gradeType, Integer numero, String algoName) {
        this.zones = zones;
        this.answers = answers;
        this.textComments = textComments;
        this.gradedComments = gradedComments;
        this.point = point;
        this.step = step;
        this.validExpression = validExpression;
        this.gradeType = gradeType;
        this.numero = numero;
        this.algoName = algoName;
    }

    public List<ZoneDTO> getZones() {
        return this.zones;
    }

    public void setZones(List<ZoneDTO> zones) {
        this.zones = zones;
    }

    public List<Answer4QuestionDTO> getAnswers() {
        return this.answers;
    }

    public void setAnswers(List<Answer4QuestionDTO> answers) {
        this.answers = answers;
    }

    public List<TextCommentDTO> getTextComments() {
        return this.textComments;
    }

    public void setTextComments(List<TextCommentDTO> textComments) {
        this.textComments = textComments;
    }

    public List<GradedCommentDTO> getGradedComments() {
        return this.gradedComments;
    }

    public void setGradedComments(List<GradedCommentDTO> gradedComments) {
        this.gradedComments = gradedComments;
    }

    public Double getPoint() {
        return this.point;
    }

    public void setPoint(Double point) {
        this.point = point;
    }

    public Integer getStep() {
        return this.step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getValidExpression() {
        return this.validExpression;
    }

    public void setValidExpression(String validExpression) {
        this.validExpression = validExpression;
    }

    public GradeType getGradeType() {
        return this.gradeType;
    }

    public void setGradeType(GradeType gradeType) {
        this.gradeType = gradeType;
    }

    public Integer getNumero() {
        return this.numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getAlgoName() {
        return this.algoName;
    }

    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    public ZoneSameCommentDTO zones(List<ZoneDTO> zones) {
        setZones(zones);
        return this;
    }

    public ZoneSameCommentDTO answers(List<Answer4QuestionDTO> answers) {
        setAnswers(answers);
        return this;
    }

    public ZoneSameCommentDTO textComments(List<TextCommentDTO> textComments) {
        setTextComments(textComments);
        return this;
    }

    public ZoneSameCommentDTO gradedComments(List<GradedCommentDTO> gradedComments) {
        setGradedComments(gradedComments);
        return this;
    }

    public ZoneSameCommentDTO point(Double point) {
        setPoint(point);
        return this;
    }

    public ZoneSameCommentDTO step(Integer step) {
        setStep(step);
        return this;
    }

    public ZoneSameCommentDTO validExpression(String validExpression) {
        setValidExpression(validExpression);
        return this;
    }

    public ZoneSameCommentDTO gradeType(GradeType gradeType) {
        setGradeType(gradeType);
        return this;
    }

    public ZoneSameCommentDTO numero(Integer numero) {
        setNumero(numero);
        return this;
    }

    public ZoneSameCommentDTO algoName(String algoName) {
        setAlgoName(algoName);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ZoneSameCommentDTO)) {
            return false;
        }
        ZoneSameCommentDTO zoneSameCommentDTO = (ZoneSameCommentDTO) o;
        return Objects.equals(zones, zoneSameCommentDTO.zones) && Objects.equals(answers, zoneSameCommentDTO.answers) && Objects.equals(textComments, zoneSameCommentDTO.textComments) && Objects.equals(gradedComments, zoneSameCommentDTO.gradedComments) && Objects.equals(point, zoneSameCommentDTO.point) && Objects.equals(step, zoneSameCommentDTO.step) && Objects.equals(validExpression, zoneSameCommentDTO.validExpression) && Objects.equals(gradeType, zoneSameCommentDTO.gradeType) && Objects.equals(numero, zoneSameCommentDTO.numero) && Objects.equals(algoName, zoneSameCommentDTO.algoName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zones, answers, textComments, gradedComments, point, step, validExpression, gradeType, numero, algoName);
    }

    @Override
    public String toString() {
        return "{" +
            " zones='" + getZones() + "'" +
            ", answers='" + getAnswers() + "'" +
            ", textComments='" + getTextComments() + "'" +
            ", gradedComments='" + getGradedComments() + "'" +
            ", point='" + getPoint() + "'" +
            ", step='" + getStep() + "'" +
            ", validExpression='" + getValidExpression() + "'" +
            ", gradeType='" + getGradeType() + "'" +
            ", numero='" + getNumero() + "'" +
            ", algoName='" + getAlgoName() + "'" +
            "}";
    }


}
