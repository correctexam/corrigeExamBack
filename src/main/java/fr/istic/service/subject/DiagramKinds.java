package fr.istic.service.subject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Text diagrams rendered by Kroki for the Typst subjects (T25c, ADR 0004). Pure.
 *
 * <p>Rendered SVGs are kept in a content-addressed cache shared by all the subjects:
 * {@code diagram-cache/{kind}/{sha256}.svg}, where the hash covers the kind and the source. The same diagram is
 * rendered once, a subject still being written (no exam yet) can use it, and a key reveals nothing of the source.
 * The cache has no owner: the orphan detector does not scan it.</p>
 */
public final class DiagramKinds {

    /** Kinds accepted, named as in Kroki (and as the language of the Typst raw block). */
    public static final Set<String> KINDS = Set.of("plantuml", "c4plantuml", "mermaid", "d2", "nomnoml");

    public static final String CACHE_PREFIX = "diagram-cache/";

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    private DiagramKinds() {
    }

    public static Optional<String> normalizeKind(String kind) {
        return kind != null && KINDS.contains(kind) ? Optional.of(kind) : Optional.empty();
    }

    /** SHA-256 (hex) of {@code kind + "\n" + source}, the identity of a rendered diagram. */
    public static String hash(String kind, String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest((kind + "\n" + source).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static String cacheKey(String kind, String sha256) {
        return CACHE_PREFIX + kind + "/" + sha256 + ".svg";
    }

    public static boolean isSha256(String value) {
        return value != null && SHA256.matcher(value).matches();
    }
}
