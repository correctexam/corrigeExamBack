package fr.istic.service.subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/** Unit tests of {@link SubjectPaths}: path validation and storage keys of subject files (T25c). */
class SubjectPathsTest {

    @Test
    void acceptsRelativePathsWithKnownExtensions() {
        assertEquals(Optional.of("main.typ"), SubjectPaths.normalize("main.typ"));
        assertEquals(Optional.of("main.typ"), SubjectPaths.normalize("./main.typ"));
        assertEquals(Optional.of("code/Main.java"), SubjectPaths.normalize("code/Main.java"));
        assertEquals(Optional.of("img/schema_v2.png"), SubjectPaths.normalize("img/schema_v2.png"));
        assertEquals(Optional.of("uml/classes.puml"), SubjectPaths.normalize("uml/classes.puml"));
    }

    @Test
    void rejectsTraversalAbsoluteAndOddPaths() {
        for (String p : new String[] { null, "", "/etc/passwd.txt", "../main.typ", "a/../main.typ", "a/./b.typ",
                "a//b.typ", "dir/", ".hidden.typ", "-x.typ", "a b.typ", "a\\b.typ", "main", "script.sh", "page.html",
                "a/b/c/d/e/f.typ", "x".repeat(201) + ".typ" }) {
            assertTrue(SubjectPaths.normalize(p).isEmpty(), String.valueOf(p));
        }
    }

    @Test
    void depthLimitIsInclusive() {
        assertTrue(SubjectPaths.normalize("a/b/c/d/e.typ").isPresent());
    }

    @Test
    void contentTypesAndTextFlag() {
        assertTrue(SubjectPaths.isText("main.typ"));
        assertTrue(SubjectPaths.isText("data.json"));
        assertTrue(SubjectPaths.isText("code/Main.java"));
        assertFalse(SubjectPaths.isText("img/a.png"));
        assertEquals("image/png", SubjectPaths.contentType("img/a.png"));
        assertEquals("image/svg+xml", SubjectPaths.contentType("img/a.svg"));
    }

    @Test
    void storageKeysFollowTheOwnerConvention() {
        assertEquals("subject/42/files/code/Main.java", SubjectPaths.storageKey(42, "code/Main.java"));
        assertEquals("subject/42/", SubjectPaths.subjectPrefix(42));
        assertTrue(SubjectPaths.storageKey(42, "main.typ").startsWith(SubjectPaths.subjectPrefix(42)));
    }
}
