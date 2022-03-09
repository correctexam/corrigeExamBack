package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class FinalResultMapperTest {

    private FinalResultMapper finalResultMapper;

    @BeforeEach
    public void setUp() {
        finalResultMapper = new FinalResultMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(finalResultMapper.fromId(id).id).isEqualTo(id);
        assertThat(finalResultMapper.fromId(null)).isNull();
    }
}
