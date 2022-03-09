package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class StudentMapperTest {

    private StudentMapper studentMapper;

    @BeforeEach
    public void setUp() {
        studentMapper = new StudentMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(studentMapper.fromId(id).id).isEqualTo(id);
        assertThat(studentMapper.fromId(null)).isNull();
    }
}
