package fr.istic.service.subject;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import fr.istic.service.dto.CopyVersionDTO;
import fr.istic.service.dto.ExamVersionsRequestDTO;

/**
 * Rules of the versions of an exam (T25e, ADR 0005). Pure logic, shared by the service and its tests.
 *
 * <p>The server does not compile Typst: it checks the shape of what the front sends (contiguous indices, labels,
 * same number of pages, layout of format 1 for the right version) and keeps it. The checks that the versions can be
 * rebuilt in the layout of version A are made by the front ({@code subject-versions.ts}) and again by the
 * reconstruction.</p>
 */
public final class VersionRules {

    public static final int MAX_VERSIONS = 6;
    public static final String LABELS = "ABCDEF";
    public static final int LAYOUT_FORMAT = 1;
    /** Characters of a serialized plan or layout (a layout lists every zone and every QCM box). */
    public static final int MAX_JSON_CHARS = 1_000_000;
    public static final String PREFIX = "version/";

    private VersionRules() {
    }

    public static String label(int index) {
        return String.valueOf(LABELS.charAt(index));
    }

    /** Key of the PDF of a version: {@code version/{examId}/{index}.pdf} (owner: the exam). */
    public static String storageKey(long examId, int index) {
        return PREFIX + examId + "/" + index + ".pdf";
    }

    public static String examPrefix(long examId) {
        return PREFIX + examId + "/";
    }

    /** Why a set of versions is refused, empty when it is valid. */
    public static Optional<String> check(ExamVersionsRequestDTO request) {
        if (request == null || request.versions() == null || request.versions().isEmpty()) {
            return Optional.of("at least one version");
        }
        if (request.seed() == null || request.seed() < 0) {
            return Optional.of("seed must be a positive number");
        }
        List<ExamVersionsRequestDTO.Version> versions = request.versions();
        if (versions.size() > MAX_VERSIONS) {
            return Optional.of("at most " + MAX_VERSIONS + " versions");
        }
        Integer pages = versions.get(0) == null ? null : versions.get(0).pages();
        for (int i = 0; i < versions.size(); i++) {
            ExamVersionsRequestDTO.Version v = versions.get(i);
            String name = "version " + label(i);
            if (v == null) {
                return Optional.of(name + ": missing");
            }
            if (v.index() == null || v.index() != i) {
                return Optional.of(name + ": index must be " + i);
            }
            if (v.label() != null && !v.label().equals(label(i))) {
                return Optional.of(name + ": label must be " + label(i));
            }
            if (v.pages() == null || v.pages() < 1 || v.pages() > EditionRules.MAX_PAGES_PER_COPY) {
                return Optional.of(name + ": pages must be between 1 and " + EditionRules.MAX_PAGES_PER_COPY);
            }
            if (!v.pages().equals(pages)) {
                return Optional.of(name + ": all the versions must have the same number of pages");
            }
            if (v.plan() == null || !v.plan().isObject()) {
                return Optional.of(name + ": plan must be a JSON object");
            }
            Optional<String> layout = checkLayout(v.layout(), i, v.pages());
            if (layout.isPresent()) {
                return Optional.of(name + ": " + layout.get());
            }
            if (chars(v.plan()) > MAX_JSON_CHARS || chars(v.layout()) > MAX_JSON_CHARS) {
                return Optional.of(name + ": plan or layout larger than " + MAX_JSON_CHARS + " characters");
            }
        }
        return Optional.empty();
    }

    private static Optional<String> checkLayout(JsonNode layout, int index, int pages) {
        if (layout == null || !layout.isObject()) {
            return Optional.of("layout must be a JSON object");
        }
        if (layout.path("format").asInt(-1) != LAYOUT_FORMAT) {
            return Optional.of("layout format must be " + LAYOUT_FORMAT);
        }
        if (layout.path("version").asInt(-1) != index) {
            return Optional.of("layout is not the one of this version");
        }
        if (layout.path("pages").asInt(-1) != pages) {
            return Optional.of("layout pages differ from the version pages");
        }
        for (String list : List.of("parts", "boxes")) {
            if (!layout.path(list).isArray()) {
                return Optional.of("layout." + list + " must be a list");
            }
        }
        if (!layout.path("identity").isObject()) {
            return Optional.of("layout.identity must be an object");
        }
        return Optional.empty();
    }

    private static int chars(JsonNode node) {
        return node == null ? 0 : node.toString().length();
    }

    /** True for the content of a PDF file. */
    public static boolean isPdf(byte[] content) {
        return content != null && content.length > 5
                && new String(content, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-");
    }

    /**
     * Why a print run of {@code versions} versions is refused, empty when it is valid.
     *
     * @param versions     requested number of versions (null: 1)
     * @param pagesPerCopy pages of a copy in the print run
     * @param pages        pages of the versions of the exam (null when it has none)
     * @param withPdf      indices of the versions of the exam whose PDF is stored
     */
    public static Optional<String> checkEdition(Integer versions, int pagesPerCopy, Integer pages, Set<Integer> withPdf) {
        int k = versions == null ? 1 : versions;
        if (k < 1 || k > MAX_VERSIONS) {
            return Optional.of("versions must be between 1 and " + MAX_VERSIONS);
        }
        if (k == 1) {
            return Optional.empty();
        }
        for (int i = 0; i < k; i++) {
            if (!withPdf.contains(i)) {
                return Optional.of("version " + label(i) + " is not stored with its PDF");
            }
        }
        if (pages == null || pages != pagesPerCopy) {
            return Optional.of("pagesPerCopy must be the pages of the versions (" + pages + ")");
        }
        return Optional.empty();
    }

    /** Copies of a scan: far more than an amphitheatre, never unbounded. */
    public static final int MAX_COPY_VERSIONS = 20000;

    /** Why a list of copy versions is refused (T25e6b), empty when it is valid. */
    public static Optional<String> checkCopyVersions(List<CopyVersionDTO> copies) {
        if (copies == null) {
            return Optional.of("a list of copies");
        }
        if (copies.size() > MAX_COPY_VERSIONS) {
            return Optional.of("at most " + MAX_COPY_VERSIONS + " copies");
        }
        Set<Integer> pages = new HashSet<>();
        for (var c : copies) {
            if (c == null || c.firstPage() == null || c.firstPage() < 1) {
                return Optional.of("firstPage must be a page of the scan (from 1)");
            }
            if (c.label() == null || c.label().length() != 1 || LABELS.indexOf(c.label()) < 0) {
                return Optional.of("copy at page " + c.firstPage() + ": label must be one of " + LABELS);
            }
            if (!pages.add(c.firstPage())) {
                return Optional.of("copy at page " + c.firstPage() + " given twice");
            }
        }
        return Optional.empty();
    }

    /** Version of copy {@code copy} (from 1) in a print run of {@code versions} versions: (copy - 1) mod versions. */
    public static int versionOfCopy(int copy, int versions) {
        return Math.floorMod(copy - 1, Math.max(1, versions));
    }
}
