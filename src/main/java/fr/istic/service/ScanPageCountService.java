package fr.istic.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Scan;
import fr.istic.service.ScanPageCountException.Reason;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Number of pages of a scan PDF.
 * <p>
 * The count is stored in {@code scan.page_count} when the PDF is uploaded or merged ({@link ScanService}), so reading it
 * is a database lookup. For a scan uploaded before that column existed, the PDF is copied once to a temporary file,
 * counted and the result stored. Counting only reads the cross-reference table and the page tree of the PDF
 * (PDFBox, no rendering): it is document-level PDF handling, allowed in the JVM.
 * <p>
 * No method here is transactional: the storage download must not hold a database transaction, so the reads and the
 * write of the count use short transactions of their own.
 */
@ApplicationScoped
public class ScanPageCountService {

    private static final Logger log = LoggerFactory.getLogger(ScanPageCountService.class);

    @Inject
    FichierS3Service fichierS3Service;

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    /** Number of PDFs that may be downloaded and counted at the same time. */
    @ConfigProperty(name = "correctexam.pagecount.max-concurrent", defaultValue = "2")
    int maxConcurrent;

    /** How long a request waits for a free counting slot before giving up. */
    @ConfigProperty(name = "correctexam.pagecount.wait-seconds", defaultValue = "30")
    long waitSeconds;

    private Semaphore permits;

    @PostConstruct
    void init() {
        permits = new Semaphore(Math.max(1, maxConcurrent), true);
    }

    /**
     * Counts the pages of a PDF file without loading it in memory (PDFBox with a temporary-file stream cache).
     *
     * @throws IOException when the file is not a readable PDF
     */
    public static int countPages(Path pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(pdf.toFile()),
                IOUtils.createTempFileOnlyStreamCache())) {
            return document.getNumberOfPages();
        }
    }

    /**
     * Same as {@link #countPages(Path)}, but returns null (and logs a warning) when the file cannot be read: an upload
     * must not fail because its pages could not be counted; the count is then computed again on demand.
     */
    public static Integer countPagesOrNull(Path pdf, long scanId) {
        try {
            return countPages(pdf);
        } catch (IOException | RuntimeException e) {
            log.warn("Could not count the pages of scan {}: {}", scanId, e.toString());
            return null;
        }
    }

    /**
     * Returns the number of pages of the scan, computing and storing it when it is not known yet.
     *
     * @throws ScanPageCountException when the scan or its PDF does not exist, the PDF cannot be read, or the
     *                                storage is unavailable
     */
    public int getPageCount(long scanId) {
        Integer known = readPageCount(scanId);
        if (known != null) {
            return known;
        }
        acquirePermit(scanId);
        try {
            // Another request may have computed it while this one was waiting
            known = readPageCount(scanId);
            if (known != null) {
                return known;
            }
            int count = computePageCount(scanId);
            QuarkusTransaction.requiringNew().run(() -> {
                Scan scan = Scan.findById(scanId);
                if (scan != null) {
                    scan.pageCount = count;
                }
            });
            log.debug("Scan {} has {} pages (computed on demand)", scanId, count);
            return count;
        } finally {
            permits.release();
        }
    }

    private void acquirePermit(long scanId) {
        try {
            if (!permits.tryAcquire(waitSeconds, TimeUnit.SECONDS)) {
                throw new ScanPageCountException(Reason.UNAVAILABLE,
                        "Too many page counts in progress, could not count the pages of scan " + scanId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScanPageCountException(Reason.UNAVAILABLE, "Interrupted while waiting to count scan " + scanId, e);
        }
    }

    /** The stored count, or null when it is not known yet. */
    private Integer readPageCount(long scanId) {
        Integer[] result = new Integer[1];
        boolean exists = QuarkusTransaction.requiringNew().call(() -> {
            Scan scan = Scan.findById(scanId);
            if (scan == null) {
                return false;
            }
            result[0] = scan.pageCount;
            return true;
        });
        if (!exists) {
            throw new ScanPageCountException(Reason.SCAN_NOT_FOUND, "Scan " + scanId + " does not exist");
        }
        return result[0];
    }

    private int computePageCount(long scanId) {
        Path tmp;
        try {
            tmp = Files.createTempFile("pagecount" + scanId + "-", ".pdf");
        } catch (IOException e) {
            throw new ScanPageCountException(Reason.UNAVAILABLE, "Could not create a temporary file", e);
        }
        try {
            copyScanTo(scanId, tmp);
            try {
                return countPages(tmp);
            } catch (IOException e) {
                throw new ScanPageCountException(Reason.INVALID_PDF,
                        "The file of scan " + scanId + " is not a readable PDF", e);
            }
        } finally {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException e) {
                log.warn("Could not delete temporary file {}: {}", tmp, e.toString());
            }
        }
    }

    /** Copies the PDF of the scan (object storage, or the legacy {@code scan.content} column) to {@code target}. */
    private void copyScanTo(long scanId, Path target) {
        if (uses3) {
            String key = "scan/" + scanId + ".pdf";
            try {
                if (!fichierS3Service.isObjectExist(key)) {
                    throw new ScanPageCountException(Reason.FILE_NOT_FOUND, "Scan " + scanId + " has no PDF");
                }
                try (InputStream in = fichierS3Service.getObject(key)) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (ScanPageCountException e) {
                throw e;
            } catch (Exception e) {
                // MinIO errors come as IllegalStateException / RuntimeException, network errors as IOException
                throw new ScanPageCountException(Reason.UNAVAILABLE, "Could not read " + key + " from the storage", e);
            }
        } else {
            byte[] content = QuarkusTransaction.requiringNew().call(() -> {
                Scan scan = Scan.findById(scanId);
                return scan != null ? scan.content : null;
            });
            if (content == null || content.length == 0) {
                throw new ScanPageCountException(Reason.FILE_NOT_FOUND, "Scan " + scanId + " has no PDF");
            }
            try {
                Files.write(target, content);
            } catch (IOException e) {
                throw new ScanPageCountException(Reason.UNAVAILABLE, "Could not write a temporary file", e);
            }
        }
    }
}
