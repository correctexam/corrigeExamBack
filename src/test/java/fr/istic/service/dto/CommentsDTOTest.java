package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class CommentsDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CommentsDTO.class);
        CommentsDTO commentsDTO1 = new CommentsDTO();
        commentsDTO1.id = 1L;
        CommentsDTO commentsDTO2 = new CommentsDTO();
        assertThat(commentsDTO1).isNotEqualTo(commentsDTO2);
        commentsDTO2.id = commentsDTO1.id;
        assertThat(commentsDTO1).isEqualTo(commentsDTO2);
        commentsDTO2.id = 2L;
        assertThat(commentsDTO1).isNotEqualTo(commentsDTO2);
        commentsDTO1.id = null;
        assertThat(commentsDTO1).isNotEqualTo(commentsDTO2);
    }
}
