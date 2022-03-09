package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class ScanTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Scan.class);
        Scan scan1 = new Scan();
        scan1.id = 1L;
        Scan scan2 = new Scan();
        scan2.id = scan1.id;
        assertThat(scan1).isEqualTo(scan2);
        scan2.id = 2L;
        assertThat(scan1).isNotEqualTo(scan2);
        scan1.id = null;
        assertThat(scan1).isNotEqualTo(scan2);
    }
}
