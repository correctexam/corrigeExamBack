package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GradedCommentMapperTest {

    private GradedCommentMapper gradedCommentMapper;

    @BeforeEach
    public void setUp() {
        gradedCommentMapper = new GradedCommentMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(gradedCommentMapper.fromId(id).id).isEqualTo(id);
        assertThat(gradedCommentMapper.fromId(null)).isNull();
    }
}
