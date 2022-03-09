package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class StudentResponseMapperTest {

    private StudentResponseMapper studentResponseMapper;

    @BeforeEach
    public void setUp() {
        studentResponseMapper = new StudentResponseMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(studentResponseMapper.fromId(id).id).isEqualTo(id);
        assertThat(studentResponseMapper.fromId(null)).isNull();
    }
}
