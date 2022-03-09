package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class ZoneDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ZoneDTO.class);
        ZoneDTO zoneDTO1 = new ZoneDTO();
        zoneDTO1.id = 1L;
        ZoneDTO zoneDTO2 = new ZoneDTO();
        assertThat(zoneDTO1).isNotEqualTo(zoneDTO2);
        zoneDTO2.id = zoneDTO1.id;
        assertThat(zoneDTO1).isEqualTo(zoneDTO2);
        zoneDTO2.id = 2L;
        assertThat(zoneDTO1).isNotEqualTo(zoneDTO2);
        zoneDTO1.id = null;
        assertThat(zoneDTO1).isNotEqualTo(zoneDTO2);
    }
}
