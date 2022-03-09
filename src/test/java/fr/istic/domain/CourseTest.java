package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class CourseTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Course.class);
        Course course1 = new Course();
        course1.id = 1L;
        Course course2 = new Course();
        course2.id = course1.id;
        assertThat(course1).isEqualTo(course2);
        course2.id = 2L;
        assertThat(course1).isNotEqualTo(course2);
        course1.id = null;
        assertThat(course1).isNotEqualTo(course2);
    }
}
