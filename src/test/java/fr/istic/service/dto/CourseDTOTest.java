package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class CourseDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CourseDTO.class);
        CourseDTO courseDTO1 = new CourseDTO();
        courseDTO1.id = 1L;
        CourseDTO courseDTO2 = new CourseDTO();
        assertThat(courseDTO1).isNotEqualTo(courseDTO2);
        courseDTO2.id = courseDTO1.id;
        assertThat(courseDTO1).isEqualTo(courseDTO2);
        courseDTO2.id = 2L;
        assertThat(courseDTO1).isNotEqualTo(courseDTO2);
        courseDTO1.id = null;
        assertThat(courseDTO1).isNotEqualTo(courseDTO2);
    }
}
