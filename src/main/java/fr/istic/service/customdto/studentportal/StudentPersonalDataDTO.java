package fr.istic.service.customdto.studentportal;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Personal data held on the student (right of access, GDPR article 15). */
@RegisterForReflection
@Schema(description = "Personal data held by CorrectExam on the student of the session.")
public class StudentPersonalDataDTO {
    public String email;
    public List<StudentRecord> records = new ArrayList<>();
    public List<StudentSheetSummaryDTO> sheets = new ArrayList<>();

    @RegisterForReflection
    @Schema(description = "One student record (one per course the student is enrolled in).")
    public static class StudentRecord {
        public String name;
        public String firstname;
        public String ine;
        public String mail;
        public String caslogin;
        public List<String> courses = new ArrayList<>();
        public List<String> groups = new ArrayList<>();
    }
}
