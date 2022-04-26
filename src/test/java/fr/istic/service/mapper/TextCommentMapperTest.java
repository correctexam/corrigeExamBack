package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TextCommentMapperTest {

    private TextCommentMapper textCommentMapper;

    @BeforeEach
    public void setUp() {
        textCommentMapper = new TextCommentMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(textCommentMapper.fromId(id).id).isEqualTo(id);
        assertThat(textCommentMapper.fromId(null)).isNull();
    }
}
