package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class QuestionTypeDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(QuestionTypeDTO.class);
        QuestionTypeDTO questionTypeDTO1 = new QuestionTypeDTO();
        questionTypeDTO1.id = 1L;
        QuestionTypeDTO questionTypeDTO2 = new QuestionTypeDTO();
        assertThat(questionTypeDTO1).isNotEqualTo(questionTypeDTO2);
        questionTypeDTO2.id = questionTypeDTO1.id;
        assertThat(questionTypeDTO1).isEqualTo(questionTypeDTO2);
        questionTypeDTO2.id = 2L;
        assertThat(questionTypeDTO1).isNotEqualTo(questionTypeDTO2);
        questionTypeDTO1.id = null;
        assertThat(questionTypeDTO1).isNotEqualTo(questionTypeDTO2);
    }
}
