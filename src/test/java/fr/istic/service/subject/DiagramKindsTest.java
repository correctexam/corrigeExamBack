package fr.istic.service.subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/** Unit tests of {@link DiagramKinds}: allowed Kroki kinds and content-addressed cache keys (T25c). */
class DiagramKindsTest {

    @Test
    void onlyAllowedKindsAreAccepted() {
        assertEquals(Optional.of("plantuml"), DiagramKinds.normalizeKind("plantuml"));
        assertEquals(Optional.of("mermaid"), DiagramKinds.normalizeKind("mermaid"));
        assertTrue(DiagramKinds.normalizeKind("Mermaid").isEmpty());
        assertTrue(DiagramKinds.normalizeKind("graphviz/../x").isEmpty());
        assertTrue(DiagramKinds.normalizeKind("ditaa").isEmpty());
        assertTrue(DiagramKinds.normalizeKind(null).isEmpty());
    }

    @Test
    void hashDependsOnKindAndSource() {
        String h = DiagramKinds.hash("plantuml", "@startuml\nA -> B\n@enduml");
        assertTrue(DiagramKinds.isSha256(h));
        assertEquals(h, DiagramKinds.hash("plantuml", "@startuml\nA -> B\n@enduml"));
        assertNotEquals(h, DiagramKinds.hash("c4plantuml", "@startuml\nA -> B\n@enduml"));
        assertNotEquals(h, DiagramKinds.hash("plantuml", "@startuml\nA -> C\n@enduml"));
    }

    @Test
    void cacheKeyIsSharedAndContentAddressed() {
        String h = DiagramKinds.hash("d2", "a -> b");
        assertEquals("diagram-cache/d2/" + h + ".svg", DiagramKinds.cacheKey("d2", h));
        assertFalse(DiagramKinds.isSha256("../../scan/1.pdf"));
        assertFalse(DiagramKinds.isSha256(h.toUpperCase()));
    }
}
