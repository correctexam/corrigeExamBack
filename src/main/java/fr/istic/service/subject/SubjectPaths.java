package fr.istic.service.subject;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Paths of the files of a Typst subject (T25c, ADR 0004). Pure: no database, no storage.
 *
 * <p>A path is relative to the subject root, made of 1 to {@value #MAX_DEPTH} segments of letters, digits, {@code .},
 * {@code _} and {@code -}, with an allowed extension. The storage key of a file is
 * {@code subject/{examId}/files/{path}}: the exam id is in the key so that the orphan detector can tell its owner.</p>
 */
public final class SubjectPaths {

    public static final int MAX_DEPTH = 5;
    public static final int MAX_LENGTH = 200;

    private static final Pattern SEGMENT = Pattern.compile("[A-Za-z0-9_][A-Za-z0-9._-]{0,99}");

    /** Allowed extensions and the content type stored for them. */
    private static final Map<String, String> TYPES = Map.ofEntries(
        Map.entry("typ", "text/plain; charset=utf-8"),
        Map.entry("txt", "text/plain; charset=utf-8"),
        Map.entry("md", "text/plain; charset=utf-8"),
        Map.entry("csv", "text/plain; charset=utf-8"),
        Map.entry("json", "application/json"),
        Map.entry("yaml", "text/plain; charset=utf-8"),
        Map.entry("yml", "text/plain; charset=utf-8"),
        Map.entry("xml", "text/plain; charset=utf-8"),
        Map.entry("java", "text/plain; charset=utf-8"),
        Map.entry("py", "text/plain; charset=utf-8"),
        Map.entry("c", "text/plain; charset=utf-8"),
        Map.entry("h", "text/plain; charset=utf-8"),
        Map.entry("cpp", "text/plain; charset=utf-8"),
        Map.entry("js", "text/plain; charset=utf-8"),
        Map.entry("ts", "text/plain; charset=utf-8"),
        Map.entry("sql", "text/plain; charset=utf-8"),
        Map.entry("puml", "text/plain; charset=utf-8"),
        Map.entry("dot", "text/plain; charset=utf-8"),
        Map.entry("mmd", "text/plain; charset=utf-8"),
        Map.entry("d2", "text/plain; charset=utf-8"),
        Map.entry("svg", "image/svg+xml"),
        Map.entry("png", "image/png"),
        Map.entry("jpg", "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"),
        Map.entry("gif", "image/gif"),
        Map.entry("webp", "image/webp"));

    private SubjectPaths() {
    }

    /**
     * Returns the normalised path, or empty when it is not allowed (absolute, {@code ..}, unknown extension, …).
     * Normalisation only removes a leading {@code ./}: the same file always has the same key.
     */
    public static Optional<String> normalize(String path) {
        if (path == null) {
            return Optional.empty();
        }
        String p = path.startsWith("./") ? path.substring(2) : path;
        if (p.isEmpty() || p.length() > MAX_LENGTH || p.startsWith("/") || p.endsWith("/") || p.contains("//")) {
            return Optional.empty();
        }
        String[] segments = p.split("/");
        if (segments.length > MAX_DEPTH) {
            return Optional.empty();
        }
        for (String s : segments) {
            if (!SEGMENT.matcher(s).matches() || s.equals(".") || s.equals("..")) {
                return Optional.empty();
            }
        }
        return extension(p).filter(TYPES::containsKey).map(e -> p);
    }

    /** Content type stored for an allowed path. */
    public static String contentType(String normalizedPath) {
        return extension(normalizedPath).map(TYPES::get).orElse("application/octet-stream");
    }

    /** True for the files the editor shows as text (sources, code, diagram sources). */
    public static boolean isText(String normalizedPath) {
        String type = contentType(normalizedPath);
        return type.startsWith("text/") || type.equals("application/json");
    }

    public static String storageKey(long examId, String normalizedPath) {
        return filesPrefix(examId) + normalizedPath;
    }

    public static String filesPrefix(long examId) {
        return "subject/" + examId + "/files/";
    }

    public static String subjectPrefix(long examId) {
        return "subject/" + examId + "/";
    }

    private static Optional<String> extension(String path) {
        int slash = path.lastIndexOf('/');
        int dot = path.lastIndexOf('.');
        if (dot <= slash + 1 || dot == path.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(path.substring(dot + 1).toLowerCase(Locale.ROOT));
    }
}
