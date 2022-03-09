package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class StudentResponseTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StudentResponse.class);
        StudentResponse studentResponse1 = new StudentResponse();
        studentResponse1.id = 1L;
        StudentResponse studentResponse2 = new StudentResponse();
        studentResponse2.id = studentResponse1.id;
        assertThat(studentResponse1).isEqualTo(studentResponse2);
        studentResponse2.id = 2L;
        assertThat(studentResponse1).isNotEqualTo(studentResponse2);
        studentResponse1.id = null;
        assertThat(studentResponse1).isNotEqualTo(studentResponse2);
    }
}
