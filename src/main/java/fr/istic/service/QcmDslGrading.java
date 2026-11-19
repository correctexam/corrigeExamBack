package fr.istic.service;

import java.util.Collection;

import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;

/**
 * Note of a QCM question whose valid expression is a grading DSL (ADR 0002), e.g.
 * {@code a&b|a&c=100; a|b=50; c=-25; *=-33}.
 * <p>
 * The DSL is parsed by the front-end only: when it creates an option (graded comment) of the question, it stores in
 * {@code grade} the percentage of the question points that this answer is worth, already bounded by the DSL. Here the
 * note is computed from these percentages alone, the same way as {@code question-grading.ts} does in the front-end:
 * <ol>
 * <li>percentage = sum of the percentages of the options of the answer (one option per answer in practice), bounded
 * to [-100, 100];</li>
 * <li>note = percentage × points of the question / 100, stored like a hybrid note: in hundredths of a point, × 4
 * ({@code quarternote = round(points × 400)}).</li>
 * </ol>
 * Pure class: no database access.
 */
public final class QcmDslGrading {

    public static final String QCM_ALGO_NAME = "QCM";

    /** Lowest and highest percentage of the question points an answer can be worth. */
    public static final double MIN_PERCENTAGE = -100.0;
    public static final double MAX_PERCENTAGE = 100.0;

    private QcmDslGrading() {
    }

    /** True for a QCM whose valid expression is a grading DSL (it contains '='); false for {@code a&b|a&c}. */
    public static boolean isDsl(Question question) {
        return question != null
                && question.type != null
                && QCM_ALGO_NAME.equals(question.type.algoName)
                && isDslExpression(question.validExpression);
    }

    /** Same test as {@code isQcmDslExpression} in the front-end. */
    public static boolean isDslExpression(String validExpression) {
        return validExpression != null && validExpression.contains("=");
    }

    /** Percentage of the question points the answer is worth, from the percentages stored on its options. */
    public static double percentage(Collection<GradedComment> options) {
        double sum = 0.0;
        if (options != null) {
            for (GradedComment option : options) {
                if (option != null && option.gradequarter != null) {
                    sum += option.gradequarter / 4.0;
                }
            }
        }
        return Math.max(MIN_PERCENTAGE, Math.min(MAX_PERCENTAGE, sum));
    }

    /** Note of the answer in points. */
    public static double points(Question question, Collection<GradedComment> options) {
        int quarterpoint = question.quarterpoint != null ? question.quarterpoint : 0;
        return percentage(options) * (quarterpoint / 4.0) / 100.0;
    }

    /** Value stored in {@code StudentResponse.quarternote}: points × 100 (hybrid convention) × 4. */
    public static int quarternote(Question question, Collection<GradedComment> options) {
        return (int) Math.round(points(question, options) * 400.0);
    }

    /** Points of one option (for the answer details of the result exports). */
    public static double optionPoints(Question question, GradedComment option) {
        if (option == null || option.gradequarter == null) {
            return 0.0;
        }
        int quarterpoint = question.quarterpoint != null ? question.quarterpoint : 0;
        return option.gradequarter / 4.0 * (quarterpoint / 4.0) / 100.0;
    }
}
