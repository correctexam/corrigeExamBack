package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class FinalResultTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FinalResult.class);
        FinalResult finalResult1 = new FinalResult();
        finalResult1.id = 1L;
        FinalResult finalResult2 = new FinalResult();
        finalResult2.id = finalResult1.id;
        assertThat(finalResult1).isEqualTo(finalResult2);
        finalResult2.id = 2L;
        assertThat(finalResult1).isNotEqualTo(finalResult2);
        finalResult1.id = null;
        assertThat(finalResult1).isNotEqualTo(finalResult2);
    }
}
