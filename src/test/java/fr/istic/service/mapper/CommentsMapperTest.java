package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CommentsMapperTest {

    private CommentsMapper commentsMapper;

    @BeforeEach
    public void setUp() {
        commentsMapper = new CommentsMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(commentsMapper.fromId(id).id).isEqualTo(id);
        assertThat(commentsMapper.fromId(null)).isNull();
    }
}
