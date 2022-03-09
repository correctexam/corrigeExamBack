package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class StudentResponseDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(StudentResponseDTO.class);
        StudentResponseDTO studentResponseDTO1 = new StudentResponseDTO();
        studentResponseDTO1.id = 1L;
        StudentResponseDTO studentResponseDTO2 = new StudentResponseDTO();
        assertThat(studentResponseDTO1).isNotEqualTo(studentResponseDTO2);
        studentResponseDTO2.id = studentResponseDTO1.id;
        assertThat(studentResponseDTO1).isEqualTo(studentResponseDTO2);
        studentResponseDTO2.id = 2L;
        assertThat(studentResponseDTO1).isNotEqualTo(studentResponseDTO2);
        studentResponseDTO1.id = null;
        assertThat(studentResponseDTO1).isNotEqualTo(studentResponseDTO2);
    }
}
