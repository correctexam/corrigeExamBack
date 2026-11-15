package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests of {@link ScanPageCountService#countPages(Path)}: no Quarkus boot.
 */
class ScanPageCountServiceTest {

    @TempDir
    Path dir;

    private Path pdf(String name, int pages, CompressParameters compression) throws IOException {
        Path file = dir.resolve(name);
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            document.save(file.toFile(), compression);
        }
        return file;
    }

    @Test
    void countsAPdfWithAClassicCrossReferenceTable() throws IOException {
        assertEquals(3, ScanPageCountService.countPages(pdf("classic.pdf", 3, CompressParameters.NO_COMPRESSION)));
    }

    @Test
    void countsAPdfWithCompressedObjectStreams() throws IOException {
        // What most scanners and PDFBox 3 produce: cross-reference and page tree in object streams
        assertEquals(7, ScanPageCountService.countPages(pdf("compressed.pdf", 7, CompressParameters.DEFAULT_COMPRESSION)));
    }

    @Test
    void countsAMergedPdfLikeScanServiceDoes() throws IOException {
        Path first = pdf("first.pdf", 2, CompressParameters.DEFAULT_COMPRESSION);
        Path second = pdf("second.pdf", 5, CompressParameters.NO_COMPRESSION);
        Path merged = dir.resolve("merged.pdf");
        try (PDDocument src = Loader.loadPDF(new RandomAccessReadBufferedFile(first.toFile()),
                IOUtils.createTempFileOnlyStreamCache());
                PDDocument dst = Loader.loadPDF(new RandomAccessReadBufferedFile(second.toFile()),
                        IOUtils.createTempFileOnlyStreamCache())) {
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDocumentMergeMode(PDFMergerUtility.DocumentMergeMode.PDFBOX_LEGACY_MODE);
            merger.appendDocument(src, dst);
            assertEquals(7, src.getNumberOfPages());
            src.save(merged.toFile());
        }
        assertEquals(7, ScanPageCountService.countPages(merged));
    }

    @Test
    void rejectsAFileThatIsNotAPdf() throws IOException {
        Path notPdf = Files.writeString(dir.resolve("notes.pdf"), "not a pdf", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> ScanPageCountService.countPages(notPdf));
        assertNull(ScanPageCountService.countPagesOrNull(notPdf, 1L));
    }

    @Test
    void returnsNullForAnEmptyOrMissingFile() throws IOException {
        Path empty = Files.createFile(dir.resolve("empty.pdf"));
        assertNull(ScanPageCountService.countPagesOrNull(empty, 1L));
        assertNull(ScanPageCountService.countPagesOrNull(dir.resolve("missing.pdf"), 1L));
    }
}
