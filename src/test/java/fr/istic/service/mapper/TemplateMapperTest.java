package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateMapperTest {

    private TemplateMapper templateMapper;

    @BeforeEach
    public void setUp() {
        templateMapper = new TemplateMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(templateMapper.fromId(id).id).isEqualTo(id);
        assertThat(templateMapper.fromId(null)).isNull();
    }
}
