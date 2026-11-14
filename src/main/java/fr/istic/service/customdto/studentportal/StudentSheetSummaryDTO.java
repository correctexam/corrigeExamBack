package fr.istic.service.customdto.studentportal;

import fr.istic.domain.enumeration.StudentAccess;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * A sheet listed in the student portal. It never carries the UUID of the sheet: that UUID opens the corrected sheet
 * through /copie/{uuid} whatever the publication of the exam.
 */
@RegisterForReflection
@Schema(description = "A sheet of the student, as listed in the student portal.")
public class StudentSheetSummaryDTO {
    @Schema(description = "Identifier of the sheet, for /api/student/me/sheets/{sheetId}.")
    public Long sheetId;
    public Long examId;
    public String examName;
    public Long courseId;
    public String courseName;

    @Schema(description = "SHEET: scanned sheet only; GRADED: corrections and grade.")
    public StudentAccess access;

    @Schema(description = "Final grade in points, when the exam is GRADED and the grade computed.")
    public Double finalNote;
}
