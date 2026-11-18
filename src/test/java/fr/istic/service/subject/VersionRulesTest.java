package fr.istic.service.subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.istic.service.dto.ExamVersionsRequestDTO;
import fr.istic.service.dto.ExamVersionsRequestDTO.Version;

class VersionRulesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode json(String text) {
        try {
            return MAPPER.readTree(text);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String name(int i) {
        return String.valueOf((char) ('A' + i));
    }

    private static JsonNode layout(int version, int pages) {
        return json("{\"format\":1,\"version\":" + version + ",\"label\":\"" + name(version) + "\",\"pages\":" + pages
                + ",\"parts\":[{\"key\":\"q1\",\"part\":1,\"zone\":{\"page\":1,\"x\":1,\"y\":2,\"w\":3,\"h\":4}}],\"boxes\":[],\"identity\":{}}");
    }

    private static Version version(int i, int pages) {
        return new Version(i, name(i), json("{\"version\":\"" + name(i) + "\",\"order\":{}}"), layout(i, pages), pages);
    }

    private static ExamVersionsRequestDTO request(int count) {
        List<Version> versions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            versions.add(version(i, 4));
        }
        return new ExamVersionsRequestDTO(123L, versions);
    }

    private static Optional<String> withVersion(int index, Version replacement) {
        List<Version> versions = new ArrayList<>(request(3).versions());
        versions.set(index, replacement);
        return VersionRules.check(new ExamVersionsRequestDTO(1L, versions));
    }

    @Test
    void acceptsOneToSixVersions() {
        for (int k = 1; k <= VersionRules.MAX_VERSIONS; k++) {
            assertEquals(Optional.empty(), VersionRules.check(request(k)), "K = " + k);
        }
    }

    @Test
    void refusesEmptyTooManyOrNoSeed() {
        assertTrue(VersionRules.check(null).isPresent());
        assertTrue(VersionRules.check(new ExamVersionsRequestDTO(1L, List.of())).isPresent());
        assertTrue(VersionRules.check(request(VersionRules.MAX_VERSIONS + 1)).isPresent());
        assertTrue(VersionRules.check(new ExamVersionsRequestDTO(null, request(2).versions())).isPresent());
        assertTrue(VersionRules.check(new ExamVersionsRequestDTO(-1L, request(2).versions())).isPresent());
    }

    @Test
    void refusesWrongIndexOrLabel() {
        Version b = version(1, 4);
        assertTrue(withVersion(1, new Version(2, "B", b.plan(), b.layout(), 4)).get().contains("index"));
        assertTrue(withVersion(1, new Version(1, "C", b.plan(), b.layout(), 4)).get().contains("label"));
        assertEquals(Optional.empty(), withVersion(1, new Version(1, null, b.plan(), b.layout(), 4)));
        assertTrue(withVersion(1, null).isPresent());
    }

    @Test
    void refusesDifferentPagesAndInvalidLayouts() {
        assertTrue(withVersion(2, version(2, 6)).get().contains("same number of pages"));
        Version c = version(2, 4);
        assertTrue(withVersion(2, new Version(2, "C", json("[]"), c.layout(), 4)).get().contains("plan"));
        assertTrue(withVersion(2, new Version(2, "C", c.plan(), null, 4)).get().contains("layout"));
        assertTrue(withVersion(2, new Version(2, "C", c.plan(), layout(1, 4), 4)).get().contains("not the one of this version"));
        assertTrue(withVersion(2, new Version(2, "C", c.plan(), layout(2, 5), 4)).get().contains("pages"));
        ObjectNode format2 = (ObjectNode) layout(2, 4);
        format2.put("format", 2);
        assertTrue(withVersion(2, new Version(2, "C", c.plan(), format2, 4)).get().contains("format"));
        ObjectNode noBoxes = (ObjectNode) layout(2, 4);
        noBoxes.remove("boxes");
        assertTrue(withVersion(2, new Version(2, "C", c.plan(), noBoxes, 4)).get().contains("boxes"));
        assertTrue(withVersion(2, new Version(2, "C", c.plan(), c.layout(), 0)).isPresent());
    }

    @Test
    void refusesHugeJson() {
        ObjectNode plan = MAPPER.createObjectNode();
        plan.put("pad", "x".repeat(VersionRules.MAX_JSON_CHARS));
        Version c = version(2, 4);
        assertTrue(withVersion(2, new Version(2, "C", plan, c.layout(), 4)).get().contains("larger"));
    }

    @Test
    void storageKeysFollowTheExam() {
        assertEquals("version/42/0.pdf", VersionRules.storageKey(42, 0));
        assertTrue(VersionRules.storageKey(42, 5).startsWith(VersionRules.examPrefix(42)));
    }

    @Test
    void recognisesPdf() {
        assertTrue(VersionRules.isPdf("%PDF-1.7\n...".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(VersionRules.isPdf("<html>".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(VersionRules.isPdf(new byte[0]));
        assertFalse(VersionRules.isPdf(null));
    }

    @Test
    void printRunOfSeveralVersionsNeedsTheirPdf() {
        assertEquals(Optional.empty(), VersionRules.checkEdition(null, 4, null, Set.of()));
        assertEquals(Optional.empty(), VersionRules.checkEdition(1, 4, null, Set.of()));
        assertEquals(Optional.empty(), VersionRules.checkEdition(3, 4, 4, Set.of(0, 1, 2, 3)));
        assertTrue(VersionRules.checkEdition(3, 4, 4, Set.of(0, 2)).get().contains("version B"));
        assertTrue(VersionRules.checkEdition(2, 4, null, Set.of()).isPresent());
        assertTrue(VersionRules.checkEdition(2, 6, 4, Set.of(0, 1)).get().contains("pagesPerCopy"));
        assertTrue(VersionRules.checkEdition(0, 4, 4, Set.of(0)).isPresent());
        assertTrue(VersionRules.checkEdition(7, 4, 4, Set.of(0)).isPresent());
    }

    @Test
    void copiesTakeTheVersionsInTurn() {
        assertEquals(List.of(0, 1, 2, 0, 1, 2, 0),
                List.of(1, 2, 3, 4, 5, 6, 7).stream().map(c -> VersionRules.versionOfCopy(c, 3)).toList());
        assertEquals(0, VersionRules.versionOfCopy(9, 1));
    }
}
