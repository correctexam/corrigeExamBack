package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class ZoneTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Zone.class);
        Zone zone1 = new Zone();
        zone1.id = 1L;
        Zone zone2 = new Zone();
        zone2.id = zone1.id;
        assertThat(zone1).isEqualTo(zone2);
        zone2.id = 2L;
        assertThat(zone1).isNotEqualTo(zone2);
        zone1.id = null;
        assertThat(zone1).isNotEqualTo(zone2);
    }
}
