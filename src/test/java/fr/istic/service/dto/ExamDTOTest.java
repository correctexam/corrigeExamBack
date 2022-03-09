package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class ExamDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExamDTO.class);
        ExamDTO examDTO1 = new ExamDTO();
        examDTO1.id = 1L;
        ExamDTO examDTO2 = new ExamDTO();
        assertThat(examDTO1).isNotEqualTo(examDTO2);
        examDTO2.id = examDTO1.id;
        assertThat(examDTO1).isEqualTo(examDTO2);
        examDTO2.id = 2L;
        assertThat(examDTO1).isNotEqualTo(examDTO2);
        examDTO1.id = null;
        assertThat(examDTO1).isNotEqualTo(examDTO2);
    }
}
