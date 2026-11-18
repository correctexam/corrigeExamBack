package fr.istic.service.subject;

import java.util.Optional;

/**
 * Rules of a print run (T25d, ADR 0004). Pure logic, shared by the service and its tests.
 *
 * <p>Page code printed by the front on every page: {@code CE1|<edition>|<copy>|<page>} (Data Matrix), copy and page
 * numbered from 1. No personal data: a code only says which copy of which print run a page is.</p>
 */
public final class EditionRules {

    public static final String CODE_FORMAT = "CE1";
    /** A print run of an amphitheatre, with margin; beyond, split into several print runs. */
    public static final int MAX_COPIES = 2000;
    public static final int MAX_PAGES_PER_COPY = 200;

    private EditionRules() {
    }

    /** Why a request is refused, empty when it is valid. */
    public static Optional<String> check(Integer copies, Integer pagesPerCopy) {
        if (copies == null || copies < 1 || copies > MAX_COPIES) {
            return Optional.of("copies must be between 1 and " + MAX_COPIES);
        }
        if (pagesPerCopy == null || pagesPerCopy < 1 || pagesPerCopy > MAX_PAGES_PER_COPY) {
            return Optional.of("pagesPerCopy must be between 1 and " + MAX_PAGES_PER_COPY);
        }
        return Optional.empty();
    }

    /** Code of a page, as printed by the front (documentation and tests of the contract). */
    public static String pageCode(long edition, int copy, int page) {
        return CODE_FORMAT + "|" + edition + "|" + copy + "|" + page;
    }
}
