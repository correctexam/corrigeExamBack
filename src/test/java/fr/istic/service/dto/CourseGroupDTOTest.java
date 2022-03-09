package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class CourseGroupDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CourseGroupDTO.class);
        CourseGroupDTO courseGroupDTO1 = new CourseGroupDTO();
        courseGroupDTO1.id = 1L;
        CourseGroupDTO courseGroupDTO2 = new CourseGroupDTO();
        assertThat(courseGroupDTO1).isNotEqualTo(courseGroupDTO2);
        courseGroupDTO2.id = courseGroupDTO1.id;
        assertThat(courseGroupDTO1).isEqualTo(courseGroupDTO2);
        courseGroupDTO2.id = 2L;
        assertThat(courseGroupDTO1).isNotEqualTo(courseGroupDTO2);
        courseGroupDTO1.id = null;
        assertThat(courseGroupDTO1).isNotEqualTo(courseGroupDTO2);
    }
}
