package fr.istic.domain.enumeration;

/**
 * What the students of an exam can see from the student portal (ADR 0003).
 * The links sent by e-mail ({@code /copie/{uuid}}) are explicit grants of the instructor and are not restricted.
 */
public enum StudentAccess {
    /** Nothing: the exam is not listed. */
    HIDDEN,
    /** The scanned sheet only, without annotations, comments or grade (default for a new exam). */
    SHEET,
    /** The corrected sheet: annotations, comments and grade. */
    GRADED
}
