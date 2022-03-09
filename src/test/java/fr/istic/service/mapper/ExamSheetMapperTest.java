package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ExamSheetMapperTest {

    private ExamSheetMapper examSheetMapper;

    @BeforeEach
    public void setUp() {
        examSheetMapper = new ExamSheetMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(examSheetMapper.fromId(id).id).isEqualTo(id);
        assertThat(examSheetMapper.fromId(null)).isNull();
    }
}
