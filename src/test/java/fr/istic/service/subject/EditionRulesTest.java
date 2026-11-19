package fr.istic.service.subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests of {@link EditionRules}: limits of a print run and format of the page codes (T25d). */
class EditionRulesTest {

    @Test
    void acceptsAPrintRunWithinTheLimits() {
        assertTrue(EditionRules.check(1, 1).isEmpty());
        assertTrue(EditionRules.check(EditionRules.MAX_COPIES, EditionRules.MAX_PAGES_PER_COPY).isEmpty());
    }

    @Test
    void refusesMissingOrOutOfRangeValues() {
        assertTrue(EditionRules.check(null, 4).orElseThrow().contains("copies"));
        assertTrue(EditionRules.check(0, 4).orElseThrow().contains("copies"));
        assertTrue(EditionRules.check(EditionRules.MAX_COPIES + 1, 4).orElseThrow().contains("copies"));
        assertTrue(EditionRules.check(30, null).orElseThrow().contains("pagesPerCopy"));
        assertTrue(EditionRules.check(30, 0).orElseThrow().contains("pagesPerCopy"));
    }

    @Test
    void pageCodesFollowTheContractOfTheFront() {
        assertEquals("CE1|12|3|4", EditionRules.pageCode(12, 3, 4));
    }
}
