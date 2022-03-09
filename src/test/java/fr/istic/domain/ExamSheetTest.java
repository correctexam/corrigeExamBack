package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class ExamSheetTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExamSheet.class);
        ExamSheet examSheet1 = new ExamSheet();
        examSheet1.id = 1L;
        ExamSheet examSheet2 = new ExamSheet();
        examSheet2.id = examSheet1.id;
        assertThat(examSheet1).isEqualTo(examSheet2);
        examSheet2.id = 2L;
        assertThat(examSheet1).isNotEqualTo(examSheet2);
        examSheet1.id = null;
        assertThat(examSheet1).isNotEqualTo(examSheet2);
    }
}
