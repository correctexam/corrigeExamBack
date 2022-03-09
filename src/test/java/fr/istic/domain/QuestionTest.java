package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class QuestionTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Question.class);
        Question question1 = new Question();
        question1.id = 1L;
        Question question2 = new Question();
        question2.id = question1.id;
        assertThat(question1).isEqualTo(question2);
        question2.id = 2L;
        assertThat(question1).isNotEqualTo(question2);
        question1.id = null;
        assertThat(question1).isNotEqualTo(question2);
    }
}
