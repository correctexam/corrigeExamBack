package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class TextCommentDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TextCommentDTO.class);
        TextCommentDTO textCommentDTO1 = new TextCommentDTO();
        textCommentDTO1.id = 1L;
        TextCommentDTO textCommentDTO2 = new TextCommentDTO();
        assertThat(textCommentDTO1).isNotEqualTo(textCommentDTO2);
        textCommentDTO2.id = textCommentDTO1.id;
        assertThat(textCommentDTO1).isEqualTo(textCommentDTO2);
        textCommentDTO2.id = 2L;
        assertThat(textCommentDTO1).isNotEqualTo(textCommentDTO2);
        textCommentDTO1.id = null;
        assertThat(textCommentDTO1).isNotEqualTo(textCommentDTO2);
    }
}
