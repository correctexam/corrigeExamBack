package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class TextCommentTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TextComment.class);
        TextComment textComment1 = new TextComment();
        textComment1.id = 1L;
        TextComment textComment2 = new TextComment();
        textComment2.id = textComment1.id;
        assertThat(textComment1).isEqualTo(textComment2);
        textComment2.id = 2L;
        assertThat(textComment1).isNotEqualTo(textComment2);
        textComment1.id = null;
        assertThat(textComment1).isNotEqualTo(textComment2);
    }
}
