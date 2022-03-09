package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class QuestionMapperTest {

    private QuestionMapper questionMapper;

    @BeforeEach
    public void setUp() {
        questionMapper = new QuestionMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(questionMapper.fromId(id).id).isEqualTo(id);
        assertThat(questionMapper.fromId(null)).isNull();
    }
}
