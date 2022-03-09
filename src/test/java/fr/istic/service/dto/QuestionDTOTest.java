package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class QuestionDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(QuestionDTO.class);
        QuestionDTO questionDTO1 = new QuestionDTO();
        questionDTO1.id = 1L;
        QuestionDTO questionDTO2 = new QuestionDTO();
        assertThat(questionDTO1).isNotEqualTo(questionDTO2);
        questionDTO2.id = questionDTO1.id;
        assertThat(questionDTO1).isEqualTo(questionDTO2);
        questionDTO2.id = 2L;
        assertThat(questionDTO1).isNotEqualTo(questionDTO2);
        questionDTO1.id = null;
        assertThat(questionDTO1).isNotEqualTo(questionDTO2);
    }
}
