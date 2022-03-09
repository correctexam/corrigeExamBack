package fr.istic.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ScanMapperTest {

    private ScanMapper scanMapper;

    @BeforeEach
    public void setUp() {
        scanMapper = new ScanMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(scanMapper.fromId(id).id).isEqualTo(id);
        assertThat(scanMapper.fromId(null)).isNull();
    }
}
