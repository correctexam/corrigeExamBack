package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class StudentDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(StudentDTO.class);
        StudentDTO studentDTO1 = new StudentDTO();
        studentDTO1.id = 1L;
        StudentDTO studentDTO2 = new StudentDTO();
        assertThat(studentDTO1).isNotEqualTo(studentDTO2);
        studentDTO2.id = studentDTO1.id;
        assertThat(studentDTO1).isEqualTo(studentDTO2);
        studentDTO2.id = 2L;
        assertThat(studentDTO1).isNotEqualTo(studentDTO2);
        studentDTO1.id = null;
        assertThat(studentDTO1).isNotEqualTo(studentDTO2);
    }
}
