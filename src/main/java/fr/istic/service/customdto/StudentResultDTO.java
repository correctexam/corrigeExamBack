package fr.istic.service.customdto;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Result of a student for an exam, extending the student identity.")
public class StudentResultDTO extends StudentDTO{
    @Schema(description = "Unique identifier of the student.")
    Long id;
    @Schema(description = "Universally unique identifier of the student.")
    String uuid;
    @Schema(description = "Student number.")
    String studentNumber;
    @Schema(description = "Result note as a string.")
    String note;
    // abi = 0 => false, abi =1 => true abi = 2 abj
    @Schema(description = "Correction indicator (0=false, 1=true, 2=abstain).")
    int abi;

    @Schema(description = "Map of question numbers to their notes.")
    Map<Integer, String> notequestions =  new HashMap<>();

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public int isAbi() {
        return abi;
    }
    public void setAbi(int abi) {
        this.abi = abi;
    }
    public int getAbi() {
        return abi;
    }

    public Map<Integer, String> getNotequestions() {
        return notequestions;
    }
    public void setNotequestions(Map<Integer, String> notequestions) {
        this.notequestions = notequestions;
    }

    public String getStudentNumber() {
        return studentNumber;
    }
    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
    @Override
    public String toString() {
        return "StudentResultDTO [abi=" + abi + ", note=" + note + ", notequestions=" + notequestions
                + ", studentNumber=" + studentNumber + ", uuid=" + uuid + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Integer.valueOf(abi).hashCode();
        result = prime * result + ((note == null) ? 0 : note.hashCode());
        result = prime * result + ((notequestions == null) ? 0 : notequestions.hashCode());
        result = prime * result + ((studentNumber == null) ? 0 : studentNumber.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StudentResultDTO other = (StudentResultDTO) obj;
        if (abi != other.abi)
            return false;
        if (note == null) {
            if (other.note != null)
                return false;
        } else if (!note.equals(other.note))
            return false;
        if (notequestions == null) {
            if (other.notequestions != null)
                return false;
        } else if (!notequestions.equals(other.notequestions))
            return false;
        if (studentNumber == null) {
            if (other.studentNumber != null)
                return false;
        } else if (!studentNumber.equals(other.studentNumber))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }




}
