package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class ExamSheetDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExamSheetDTO.class);
        ExamSheetDTO examSheetDTO1 = new ExamSheetDTO();
        examSheetDTO1.id = 1L;
        ExamSheetDTO examSheetDTO2 = new ExamSheetDTO();
        assertThat(examSheetDTO1).isNotEqualTo(examSheetDTO2);
        examSheetDTO2.id = examSheetDTO1.id;
        assertThat(examSheetDTO1).isEqualTo(examSheetDTO2);
        examSheetDTO2.id = 2L;
        assertThat(examSheetDTO1).isNotEqualTo(examSheetDTO2);
        examSheetDTO1.id = null;
        assertThat(examSheetDTO1).isNotEqualTo(examSheetDTO2);
    }
}
