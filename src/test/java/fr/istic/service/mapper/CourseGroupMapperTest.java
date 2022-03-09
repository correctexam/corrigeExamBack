package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseGroupMapperTest {

    private CourseGroupMapper courseGroupMapper;

    @BeforeEach
    public void setUp() {
        courseGroupMapper = new CourseGroupMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(courseGroupMapper.fromId(id).id).isEqualTo(id);
        assertThat(courseGroupMapper.fromId(null)).isNull();
    }
}
