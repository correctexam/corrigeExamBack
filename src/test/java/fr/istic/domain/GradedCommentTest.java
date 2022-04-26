package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class GradedCommentTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GradedComment.class);
        GradedComment gradedComment1 = new GradedComment();
        gradedComment1.id = 1L;
        GradedComment gradedComment2 = new GradedComment();
        gradedComment2.id = gradedComment1.id;
        assertThat(gradedComment1).isEqualTo(gradedComment2);
        gradedComment2.id = 2L;
        assertThat(gradedComment1).isNotEqualTo(gradedComment2);
        gradedComment1.id = null;
        assertThat(gradedComment1).isNotEqualTo(gradedComment2);
    }
}
