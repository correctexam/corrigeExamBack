package fr.istic.service.customdto;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class StudentMassDTO {
    private long course;
    private List<StudentDTO> students;

    public long getCourse() { return course; }
    public void setCourse(long value) { this.course = value; }

    public List<StudentDTO> getStudents() { return students; }
    public void setStudents(List<StudentDTO> value) { this.students = value; }
}
