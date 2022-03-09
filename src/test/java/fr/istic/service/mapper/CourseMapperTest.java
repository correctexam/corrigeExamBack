package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseMapperTest {

    private CourseMapper courseMapper;

    @BeforeEach
    public void setUp() {
        courseMapper = new CourseMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(courseMapper.fromId(id).id).isEqualTo(id);
        assertThat(courseMapper.fromId(null)).isNull();
    }
}
