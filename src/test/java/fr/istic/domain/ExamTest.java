package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class ExamTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Exam.class);
        Exam exam1 = new Exam();
        exam1.id = 1L;
        Exam exam2 = new Exam();
        exam2.id = exam1.id;
        assertThat(exam1).isEqualTo(exam2);
        exam2.id = 2L;
        assertThat(exam1).isNotEqualTo(exam2);
        exam1.id = null;
        assertThat(exam1).isNotEqualTo(exam2);
    }
}
