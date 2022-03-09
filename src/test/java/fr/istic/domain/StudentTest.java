package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class StudentTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Student.class);
        Student student1 = new Student();
        student1.id = 1L;
        Student student2 = new Student();
        student2.id = student1.id;
        assertThat(student1).isEqualTo(student2);
        student2.id = 2L;
        assertThat(student1).isNotEqualTo(student2);
        student1.id = null;
        assertThat(student1).isNotEqualTo(student2);
    }
}
