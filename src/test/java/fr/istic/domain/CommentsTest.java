package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class CommentsTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Comments.class);
        Comments comments1 = new Comments();
        comments1.id = 1L;
        Comments comments2 = new Comments();
        comments2.id = comments1.id;
        assertThat(comments1).isEqualTo(comments2);
        comments2.id = 2L;
        assertThat(comments1).isNotEqualTo(comments2);
        comments1.id = null;
        assertThat(comments1).isNotEqualTo(comments2);
    }
}
