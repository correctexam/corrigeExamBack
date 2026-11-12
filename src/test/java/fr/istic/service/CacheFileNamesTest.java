package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.OptionalLong;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

/** Plain unit tests (no Quarkus boot) of the file names accepted from clients. */
class CacheFileNamesTest {

    @Test
    void cacheFileNamesWrittenByTheFrontEndGiveTheirExam() {
        assertEquals(OptionalLong.of(42), CacheFileNames.examIdOfCacheFile("42indexdb.json"));
        assertEquals(OptionalLong.of(42), CacheFileNames.examIdOfCacheFile("42_exam_template_indexdb.json"));
        assertEquals(OptionalLong.of(42), CacheFileNames.examIdOfCacheFile("42_part_3_indexdb.json"));
        assertEquals(OptionalLong.of(7), CacheFileNames.examIdOfCacheFile("7.sqlite3"));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "indexdb.json",
            "42.json",
            "../42.sqlite3",
            "42.sqlite3/../../etc/passwd",
            "..%2F42.sqlite3",
            "42.sqlite3\0",
            "/tmp/42.sqlite3",
            "a42.sqlite3",
            "42_part_x_indexdb.json",
            "1234567890123456789.sqlite3",
        }
    )
    void otherNamesAreRefused(String name) {
        assertTrue(CacheFileNames.examIdOfCacheFile(name).isEmpty(), name);
    }

    @Test
    void nullIsRefused() {
        assertTrue(CacheFileNames.examIdOfCacheFile(null).isEmpty());
        assertFalse(CacheFileNames.isStudentPdf(null));
    }

    @Test
    void studentPdfNamesAreSheetUuids() {
        assertTrue(CacheFileNames.isStudentPdf("3f1c2b9e-6a4d-4b8e-9f00-1d2c3b4a5e6f.pdf"));
        assertFalse(CacheFileNames.isStudentPdf("../x.pdf"));
        assertFalse(CacheFileNames.isStudentPdf("x.pdf.exe"));
        assertFalse(CacheFileNames.isStudentPdf("a b.pdf"));
        assertFalse(CacheFileNames.isStudentPdf(".pdf"));
    }

    @Test
    void resolveInsideKeepsPlainNamesInTheDirectory() {
        Path dir = Path.of("/tmp/files");
        assertEquals(Path.of("/tmp/files/42.sqlite3"), CacheFileNames.resolveInside(dir, "42.sqlite3"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "..", ".", "../x", "a/b", "a\\b", "/etc/passwd", "x\0" })
    void resolveInsideRefusesAnythingElse(String name) {
        assertThrows(IllegalArgumentException.class, () -> CacheFileNames.resolveInside(Path.of("/tmp/files"), name));
    }

    @Test
    void localStorageKeysStayInTheWorkingDirectory() {
        Path base = Path.of("").toAbsolutePath().normalize();
        assertEquals(base.resolve("cache/42.sqlite3"), FichierS3Service.localPath("cache/42.sqlite3"));
        for (String key : new String[] { "cache/../../x", "../x", "/etc/passwd", "", ".", "cache/x\0" }) {
            assertThrows(IllegalArgumentException.class, () -> FichierS3Service.localPath(key), key);
        }
    }
}
