package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import fr.istic.domain.Scan;
import fr.istic.service.ScanPageCountException.Reason;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * {@link ScanPageCountService#getPageCount(long)} with the test profile ({@code correctexam.uses3=false}: the PDF is
 * in {@code scan.content}): the count is computed once, then read from {@code scan.page_count}.
 */
@QuarkusTest
class ScanPageCountServiceDbTest {

    @Inject
    ScanPageCountService service;

    private static byte[] pdf(int pages) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            document.save(out);
            return out.toByteArray();
        }
    }

    private static long createScan(byte[] content, Integer pageCount) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Scan scan = new Scan();
            scan.name = "pagecount-test";
            scan.content = content;
            scan.contentContentType = "application/pdf";
            scan.pageCount = pageCount;
            scan.persist();
            return scan.id;
        });
    }

    private static Integer storedCount(long scanId) {
        return QuarkusTransaction.requiringNew().call(() -> Scan.<Scan>findById(scanId).pageCount);
    }

    @Test
    void computesTheCountOfAnOldScanAndStoresIt() throws IOException {
        long scanId = createScan(pdf(4), null);
        assertNull(storedCount(scanId));

        assertEquals(4, service.getPageCount(scanId));
        assertEquals(4, storedCount(scanId));
    }

    @Test
    void returnsTheStoredCountWithoutReadingTheFile() {
        // The content is not a PDF: a stored count proves the file is not read again
        long scanId = createScan("not a pdf".getBytes(StandardCharsets.UTF_8), 12);
        assertEquals(12, service.getPageCount(scanId));
    }

    @Test
    void reportsMissingScansFilesAndInvalidPdfs() {
        assertEquals(Reason.SCAN_NOT_FOUND,
                assertThrows(ScanPageCountException.class, () -> service.getPageCount(Long.MAX_VALUE)).getReason());

        long noFile = createScan(null, null);
        assertEquals(Reason.FILE_NOT_FOUND,
                assertThrows(ScanPageCountException.class, () -> service.getPageCount(noFile)).getReason());

        long invalid = createScan("not a pdf".getBytes(StandardCharsets.UTF_8), null);
        assertEquals(Reason.INVALID_PDF,
                assertThrows(ScanPageCountException.class, () -> service.getPageCount(invalid)).getReason());
        assertNull(storedCount(invalid));
    }
}
