package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ZoneMapperTest {

    private ZoneMapper zoneMapper;

    @BeforeEach
    public void setUp() {
        zoneMapper = new ZoneMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(zoneMapper.fromId(id).id).isEqualTo(id);
        assertThat(zoneMapper.fromId(null)).isNull();
    }
}
