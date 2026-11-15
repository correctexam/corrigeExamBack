package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Scan;
import fr.istic.service.dto.ScanDTO;
import fr.istic.service.dto.ScanDTOContent;
import fr.istic.service.mapper.ScanContentMapper;
import fr.istic.service.mapper.ScanMapper;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility.DocumentMergeMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import jakarta.ws.rs.core.MultivaluedMap;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Transactional
public class ScanService {

    private final Logger log = LoggerFactory.getLogger(ScanService.class);

    @Inject
    ScanMapper scanMapper;

    @Inject
    ScanContentMapper scanContentMapper;

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    StorageDeletionService storageDeletionService;

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    @Transactional
    public ScanDTO persistOrUpdate(ScanDTOContent scanDTO) {
        log.debug("Request to save Scan : {}", scanDTO.id);
        var scan = scanContentMapper.toEntity(scanDTO);
        if (this.uses3) {
            /*
             * if (scanDTO.name.endsWith("indexdb.json")){
             * PanacheQuery<Scan> q = Scan.findByName(scanDTO.name);
             * long number = q.count();
             * if (number > 0){
             * Scan s = q.firstResult();
             * s.content = scan.content;
             * log.error("content length " + scan.content.length );
             * scan = Scan.persistOrUpdate(s);
             * return scanContentMapper.toDto(s);
             * } else {
             * scan = Scan.persistOrUpdate(scan);
             * return scanContentMapper.toDto(scan);
             * }
             *
             * } else {
             */
            // byte[] bytes = scanDTO.content;
            scan.content = null;
            scan = Scan.persistOrUpdate(scan);
            /*
             * try {
             *
             * fichierS3Service.putObject("scan/" + scan.id + ".pdf", bytes,
             * scanDTO.contentContentType);
             * } catch (InvalidKeyException | NoSuchAlgorithmException |
             * IllegalArgumentException | IOException e) {
             * e.printStackTrace();
             * }
             */
            ScanDTO dto = scanMapper.toDto(scan);
            // dto.content = null;
            return dto;
        } else {
            scan.content = null;
            scan = Scan.persistOrUpdate(scan);
            ScanDTO dto = scanMapper.toDto(scan);
            // dto.content = null;
            return dto;

        }

        // }

    }

    /**
     * Delete the Scan by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Scan : {}", id);
        Optional<Scan> scanop = Scan.findByIdOptional(id);
        scanop.ifPresent(scan -> {
            scan.delete();
            if (this.uses3) {
                // removed from S3 only once the deletion is committed
                this.storageDeletionService.deleteAfterCommit(List.of("scan/" + scan.id + ".pdf"));
            }
        });
    }

    /**
     * Get one scan by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ScanDTO> findOne(Long id) {
        log.debug("Request to get Scan : {}", id);
        Optional<Scan> scanop = Scan.findByIdOptional(id);
        Optional<ScanDTO> dto = scanop
                .map(scan -> scanMapper.toDto((Scan) scan));
        /*
         * if (this.uses3) {
         * if (dto.isPresent()) {
         * ScanDTOContent scan = dto.get();
         *
         * if (this.fichierS3Service.isObjectExist("scan/" + scan.id + ".pdf")) {
         * byte[] bytes;
         * try {
         * bytes = IOUtils.toByteArray(this.fichierS3Service.getObject("scan/" + scan.id
         * + ".pdf"));
         * scan.content = bytes;
         *
         * } catch (InvalidKeyException | NoSuchAlgorithmException |
         * IllegalArgumentException
         * | IOException e) {
         * e.printStackTrace();
         * }
         * } else {
         * byte[] bytes = scan.content;
         * try {
         * fichierS3Service.putObject("scan/" + scan.id + ".pdf", bytes,
         * scan.contentContentType);
         * } catch (InvalidKeyException | NoSuchAlgorithmException |
         * IllegalArgumentException
         * | IOException e) {
         * e.printStackTrace();
         * }
         * }
         * }
         * }
         */
        return dto;
    }

    /**
     * Get all the scans.
     *
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ScanDTO> findAll(Page page) {
        log.debug("Request to get all Scans");
        return new Paged<>(Scan.findAll().page(page))
                .map(scan -> scanMapper.toDto((Scan) scan));
    }

    /**
     * Get all the scans by Name.
     *
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ScanDTO> findbyName(String name, Page page) {
        log.debug("Request to get all Scans by name");
        Paged<Scan> scans = new Paged<>(Scan.findByName(name).page(page));
        Paged<ScanDTO> dtos = scans.map(scan -> scanMapper.toDto((Scan) scan));

        /*
         * if (this.uses3) {
         *
         * for (ScanDTOContent scan : dtos.content) {
         *
         * if (this.fichierS3Service.isObjectExist("scan/" + scan.id + ".pdf")) {
         * byte[] bytes;
         * try {
         * bytes = IOUtils.toByteArray(this.fichierS3Service.getObject("scan/" + scan.id
         * + ".pdf"));
         * scan.content = bytes;
         *
         * } catch (InvalidKeyException | NoSuchAlgorithmException |
         * IllegalArgumentException
         * | IOException e) {
         * e.printStackTrace();
         * }
         * } else {
         * byte[] bytes = scan.content;
         * try {
         * fichierS3Service.putObject("scan/" + scan.id + ".pdf", bytes,
         * scan.contentContentType);
         * } catch (InvalidKeyException | NoSuchAlgorithmException |
         * IllegalArgumentException
         * | IOException e) {
         * e.printStackTrace();
         * }
         *
         * }
         * }
         * }
         */
        return dtos;
    }

    /**
     * Stores the uploaded PDF as the file of the scan, or appends it to the existing file when {@code merge} is true,
     * and records the resulting number of pages in {@code scan.page_count}.
     *
     * @throws IOException      when the uploaded or the stored file cannot be read or written
     * @throws StorageException when the object storage rejects the upload
     */
    public void uploadFile(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file, long scanId,
            boolean merge) throws IOException {
        java.nio.file.Path uploaded = file.uploadedFile();
        if (merge && this.hasScanFile(scanId)) {
            mergeFile(uploaded, "application/pdf", scanId);
        } else {
            writeFile(uploaded, "application/pdf", scanId);
        }
    }

    protected String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }

    protected boolean hasScanFile(long scanId) {
        String fileName = "scan/" + scanId + ".pdf";
        return this.fichierS3Service.isObjectExist(fileName);
    }

    protected InputStream getScanFile(long scanId)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        String fileName = "scan/" + scanId + ".pdf";
        return this.fichierS3Service.getObject(fileName);
    }

    protected void mergeFile(java.nio.file.Path toadd, String contenttype, long scanId)
            throws IOException {
        String fileName = "scan/" + scanId + ".pdf";
        java.nio.file.Path scan = Files.createTempFile("scan" + scanId, ".pdf");
        java.nio.file.Path res = Files.createTempFile("res" + scanId, ".pdf");
        try {
            try (InputStream existing = this.getScanFile(scanId)) {
                FileUtils.copyInputStreamToFile(existing, scan.toFile());
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                throw new StorageException(fileName, "Could not read the scan to merge", e);
            }

            int pageCount;
            try (PDDocument src = org.apache.pdfbox.Loader.loadPDF(new RandomAccessReadBufferedFile(scan.toFile()),
                    org.apache.pdfbox.io.IOUtils.createTempFileOnlyStreamCache());
                    PDDocument dst = org.apache.pdfbox.Loader.loadPDF(new RandomAccessReadBufferedFile(toadd.toFile()),
                            org.apache.pdfbox.io.IOUtils.createTempFileOnlyStreamCache());
                    FileOutputStream fo = new FileOutputStream(res.toFile())) {
                PDFMergerUtility merger = new PDFMergerUtility();
                merger.setDocumentMergeMode(DocumentMergeMode.PDFBOX_LEGACY_MODE);
                merger.appendDocument(src, dst);
                // The merged document is already loaded: counting its pages costs nothing
                pageCount = src.getNumberOfPages();
                src.save(fo);
            }

            if (this.uses3) {
                try {
                    this.uploadObject(fileName, res.toFile().getAbsolutePath(), contenttype);
                } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                    throw new StorageException(fileName, "Could not upload the merged scan", e);
                }
            } else {
                this.updateContent(scanId, Files.readAllBytes(res));
            }
            this.updatePageCount(scanId, pageCount);
        } finally {
            Files.deleteIfExists(scan);
            Files.deleteIfExists(res);
        }
    }

    protected void writeFile(java.nio.file.Path pdf, String contenttype, long scanId)
            throws IOException {
        // Counted before the upload: the file is local, and a non-PDF file must not block the upload
        Integer pageCount = ScanPageCountService.countPagesOrNull(pdf, scanId);
        if (this.uses3) {
            String fileName = "scan/" + scanId + ".pdf";
            try {
                // Streams the uploaded file instead of loading it in memory
                this.uploadObject(fileName, pdf.toAbsolutePath().toString(), contenttype);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                throw new StorageException(fileName, "Could not upload the scan", e);
            }
        } else {
            this.updateContent(scanId, Files.readAllBytes(pdf));
        }
        // Null when the count failed: GET /api/scans/{id}/pagecount will try again
        this.updatePageCount(scanId, pageCount);
    }

    /** Records the number of pages of the scan file; null means unknown. */
    @Transactional
    protected void updatePageCount(long scanId, Integer pageCount) {
        Scan s = Scan.findById(scanId);
        if (s != null) {
            s.pageCount = pageCount;
        }
    }

    @Transactional
    protected void updateContent(long scanId, byte[] b64bytes) {
        Scan s = Scan.findById(scanId);
        s.content = b64bytes;
        s.persistOrUpdate();
    }

    protected void putObject(String name, byte[] bytes, String contenttype)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        this.fichierS3Service.putObject(name, bytes, contenttype);
    }

    protected void uploadObject(String name, String origfilename, String contenttype)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        this.fichierS3Service.uploadObject(name, origfilename, contenttype);
    }

}
