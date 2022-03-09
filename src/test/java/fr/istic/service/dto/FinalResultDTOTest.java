package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class FinalResultDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FinalResultDTO.class);
        FinalResultDTO finalResultDTO1 = new FinalResultDTO();
        finalResultDTO1.id = 1L;
        FinalResultDTO finalResultDTO2 = new FinalResultDTO();
        assertThat(finalResultDTO1).isNotEqualTo(finalResultDTO2);
        finalResultDTO2.id = finalResultDTO1.id;
        assertThat(finalResultDTO1).isEqualTo(finalResultDTO2);
        finalResultDTO2.id = 2L;
        assertThat(finalResultDTO1).isNotEqualTo(finalResultDTO2);
        finalResultDTO1.id = null;
        assertThat(finalResultDTO1).isNotEqualTo(finalResultDTO2);
    }
}
