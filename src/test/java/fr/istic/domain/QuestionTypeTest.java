package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class QuestionTypeTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(QuestionType.class);
        QuestionType questionType1 = new QuestionType();
        questionType1.id = 1L;
        QuestionType questionType2 = new QuestionType();
        questionType2.id = questionType1.id;
        assertThat(questionType1).isEqualTo(questionType2);
        questionType2.id = 2L;
        assertThat(questionType1).isNotEqualTo(questionType2);
        questionType1.id = null;
        assertThat(questionType1).isNotEqualTo(questionType2);
    }
}
