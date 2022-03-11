package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class QuestionTypeMapperTest {

    private QuestionTypeMapper questionTypeMapper;

    @BeforeEach
    public void setUp() {
        questionTypeMapper = new QuestionTypeMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(questionTypeMapper.fromId(id).id).isEqualTo(id);
        assertThat(questionTypeMapper.fromId(null)).isNull();
    }
}
