package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class ScanDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ScanDTO.class);
        ScanDTO scanDTO1 = new ScanDTO();
        scanDTO1.id = 1L;
        ScanDTO scanDTO2 = new ScanDTO();
        assertThat(scanDTO1).isNotEqualTo(scanDTO2);
        scanDTO2.id = scanDTO1.id;
        assertThat(scanDTO1).isEqualTo(scanDTO2);
        scanDTO2.id = 2L;
        assertThat(scanDTO1).isNotEqualTo(scanDTO2);
        scanDTO1.id = null;
        assertThat(scanDTO1).isNotEqualTo(scanDTO2);
    }
}
