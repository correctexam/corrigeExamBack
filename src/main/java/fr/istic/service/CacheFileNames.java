package fr.istic.service;

import java.nio.file.Path;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Names accepted for the files that clients upload to, or read from, the storage.
 * <p>
 * The names come from the client (multipart file name, path parameter): they are checked against the storage key
 * conventions before being used to build an S3 key or a path on disk.
 */
public final class CacheFileNames {

    /**
     * Exam cache files, stored under {@code cache/}: {@code {examId}indexdb.json},
     * {@code {examId}_exam_template_indexdb.json}, {@code {examId}_part_{n}_indexdb.json}, {@code {examId}.sqlite3}.
     */
    private static final Pattern EXAM_CACHE_FILE = Pattern.compile(
        "(\\d{1,18})(indexdb\\.json|_exam_template_indexdb\\.json|_part_\\d{1,6}_indexdb\\.json|\\.sqlite3)"
    );

    /** Corrected copy of a student, stored under {@code exportpdf/{examId}/}: {@code {sheet name}.pdf}. */
    private static final Pattern STUDENT_PDF_FILE = Pattern.compile("[A-Za-z0-9_-]{1,64}\\.pdf");

    private CacheFileNames() {}

    /** Exam owning the cache file {@code name}, or empty when {@code name} is not a cache file name. */
    public static OptionalLong examIdOfCacheFile(String name) {
        if (name == null) {
            return OptionalLong.empty();
        }
        Matcher matcher = EXAM_CACHE_FILE.matcher(name);
        return matcher.matches() ? OptionalLong.of(Long.parseLong(matcher.group(1))) : OptionalLong.empty();
    }

    public static boolean isStudentPdf(String name) {
        return name != null && STUDENT_PDF_FILE.matcher(name).matches();
    }

    /**
     * Resolves {@code name} in {@code directory}, refusing anything that is not a plain file name of that directory
     * (separators, {@code ..}, absolute paths).
     */
    public static Path resolveInside(Path directory, String name) {
        if (name == null || name.isEmpty() || name.contains("/") || name.contains("\\") || name.contains("\0")) {
            throw new IllegalArgumentException("Invalid file name: " + name);
        }
        Path base = directory.toAbsolutePath().normalize();
        Path resolved = base.resolve(name).normalize();
        if (!base.equals(resolved.getParent())) {
            throw new IllegalArgumentException("Invalid file name: " + name);
        }
        return resolved;
    }
}
