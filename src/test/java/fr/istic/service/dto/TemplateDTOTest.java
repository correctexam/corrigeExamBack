package fr.istic.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import fr.istic.TestUtil;

public class TemplateDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TemplateDTO.class);
        TemplateDTO templateDTO1 = new TemplateDTO();
        templateDTO1.id = 1L;
        TemplateDTO templateDTO2 = new TemplateDTO();
        assertThat(templateDTO1).isNotEqualTo(templateDTO2);
        templateDTO2.id = templateDTO1.id;
        assertThat(templateDTO1).isEqualTo(templateDTO2);
        templateDTO2.id = 2L;
        assertThat(templateDTO1).isNotEqualTo(templateDTO2);
        templateDTO1.id = null;
        assertThat(templateDTO1).isNotEqualTo(templateDTO2);
    }
}
