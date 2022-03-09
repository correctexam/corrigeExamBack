package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class CourseGroupTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CourseGroup.class);
        CourseGroup courseGroup1 = new CourseGroup();
        courseGroup1.id = 1L;
        CourseGroup courseGroup2 = new CourseGroup();
        courseGroup2.id = courseGroup1.id;
        assertThat(courseGroup1).isEqualTo(courseGroup2);
        courseGroup2.id = 2L;
        assertThat(courseGroup1).isNotEqualTo(courseGroup2);
        courseGroup1.id = null;
        assertThat(courseGroup1).isNotEqualTo(courseGroup2);
    }
}
