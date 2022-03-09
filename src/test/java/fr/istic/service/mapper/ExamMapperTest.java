package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ExamMapperTest {

    private ExamMapper examMapper;

    @BeforeEach
    public void setUp() {
        examMapper = new ExamMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(examMapper.fromId(id).id).isEqualTo(id);
        assertThat(examMapper.fromId(null)).isNull();
    }
}
