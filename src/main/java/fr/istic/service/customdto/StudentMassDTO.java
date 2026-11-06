package fr.istic.service.customdto;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "A course with its list of students, used for mass import.")
public class StudentMassDTO {
    @Schema(description = "Identifier of the course.")
    private long course;
    @Schema(description = "Students to import into the course.")
    private List<StudentDTO> students;

    public long getCourse() { return course; }
    public void setCourse(long value) { this.course = value; }

    public List<StudentDTO> getStudents() { return students; }
    public void setStudents(List<StudentDTO> value) { this.students = value; }
}
