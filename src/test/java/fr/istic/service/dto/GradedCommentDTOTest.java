package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class GradedCommentDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GradedCommentDTO.class);
        GradedCommentDTO gradedCommentDTO1 = new GradedCommentDTO();
        gradedCommentDTO1.id = 1L;
        GradedCommentDTO gradedCommentDTO2 = new GradedCommentDTO();
        assertThat(gradedCommentDTO1).isNotEqualTo(gradedCommentDTO2);
        gradedCommentDTO2.id = gradedCommentDTO1.id;
        assertThat(gradedCommentDTO1).isEqualTo(gradedCommentDTO2);
        gradedCommentDTO2.id = 2L;
        assertThat(gradedCommentDTO1).isNotEqualTo(gradedCommentDTO2);
        gradedCommentDTO1.id = null;
        assertThat(gradedCommentDTO1).isNotEqualTo(gradedCommentDTO2);
    }
}
