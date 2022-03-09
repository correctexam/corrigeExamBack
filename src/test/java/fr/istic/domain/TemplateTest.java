package fr.istic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.istic.TestUtil;
import org.junit.jupiter.api.Test;


public class TemplateTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Template.class);
        Template template1 = new Template();
        template1.id = 1L;
        Template template2 = new Template();
        template2.id = template1.id;
        assertThat(template1).isEqualTo(template2);
        template2.id = 2L;
        assertThat(template1).isNotEqualTo(template2);
        template1.id = null;
        assertThat(template1).isNotEqualTo(template2);
    }
}
