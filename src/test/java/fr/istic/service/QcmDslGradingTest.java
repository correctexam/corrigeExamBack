package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.QuestionType;

/**
 * {@link QcmDslGrading}: note of a QCM in grading DSL mode from the percentages stored on its options
 * (reference examples of ADR 0002, question on 2 points). No Quarkus boot.
 */
class QcmDslGradingTest {

    private static Question qcm(String validExpression, double points) {
        QuestionType type = new QuestionType();
        type.algoName = "QCM";
        Question q = new Question();
        q.type = type;
        q.validExpression = validExpression;
        q.quarterpoint = (int) Math.round(points * 4);
        return q;
    }

    private static GradedComment option(double percentage) {
        GradedComment g = new GradedComment();
        g.gradequarter = (int) Math.round(percentage * 4);
        return g;
    }

    private static final String DSL = "a&b|a&c=100; c=-25; *=-33; min=-100";

    @Test
    void detectsTheDslMode() {
        assertTrue(QcmDslGrading.isDsl(qcm(DSL, 2)));
        assertFalse(QcmDslGrading.isDsl(qcm("a&b|a&c", 2)));
        assertFalse(QcmDslGrading.isDsl(qcm(null, 2)));
        Question notQcm = qcm(DSL, 2);
        notQcm.type.algoName = "manuscrit";
        assertFalse(QcmDslGrading.isDsl(notQcm));
        Question noType = qcm(DSL, 2);
        noType.type = null;
        assertFalse(QcmDslGrading.isDsl(noType));
    }

    @Test
    void computesTheNoteFromThePercentageOfTheOption() {
        Question q = qcm(DSL, 2);
        assertEquals(2.0, QcmDslGrading.points(q, List.of(option(100))), 1e-9);
        assertEquals(1.0, QcmDslGrading.points(q, List.of(option(50))), 1e-9);
        assertEquals(-0.5, QcmDslGrading.points(q, List.of(option(-25))), 1e-9);
        assertEquals(0.0, QcmDslGrading.points(q, List.of()), 1e-9);
        assertEquals(0.2, QcmDslGrading.points(q, List.of(option(10))), 1e-9);
    }

    @Test
    void storesTheNoteInHundredthsOfAPointLikeHybridNotes() {
        Question q = qcm(DSL, 2);
        assertEquals(800, QcmDslGrading.quarternote(q, List.of(option(100))));
        assertEquals(400, QcmDslGrading.quarternote(q, List.of(option(50))));
        // -33 %: -0.66 point, not truncated to a quarter of a point
        assertEquals(-264, QcmDslGrading.quarternote(q, List.of(option(-33))));
    }

    @Test
    void boundsTheSumOfSeveralOptions() {
        Question q = qcm(DSL, 2);
        assertEquals(100.0, QcmDslGrading.percentage(List.of(option(100), option(50))), 1e-9);
        assertEquals(-100.0, QcmDslGrading.percentage(List.of(option(-80), option(-80))), 1e-9);
        assertEquals(2.0, QcmDslGrading.points(q, List.of(option(100), option(50))), 1e-9);
    }

    @Test
    void ignoresOptionsWithoutPercentage() {
        GradedComment noGrade = new GradedComment();
        assertEquals(50.0, QcmDslGrading.percentage(List.of(option(50), noGrade)), 1e-9);
        assertEquals(0.0, QcmDslGrading.optionPoints(qcm(DSL, 2), noGrade), 1e-9);
        assertEquals(1.0, QcmDslGrading.optionPoints(qcm(DSL, 2), option(50)), 1e-9);
    }
}
