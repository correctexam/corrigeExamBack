package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Scan;
import fr.istic.service.dto.ScanDTO;
import fr.istic.service.dto.ScanDTOContent;
import fr.istic.service.mapper.ScanContentMapper;
import fr.istic.service.mapper.ScanMapper;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility.DocumentMergeMode;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;


import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import javax.ws.rs.core.MultivaluedMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
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
            /*try {

                fichierS3Service.putObject("scan/" + scan.id + ".pdf", bytes, scanDTO.contentContentType);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
                e.printStackTrace();
            }*/
            ScanDTO dto = scanMapper.toDto(scan);
           // dto.content = null;
            return dto;
        } else {
            scan.content = null;
            scan = Scan.persistOrUpdate(scan);
            ScanDTO dto = scanMapper.toDto(scan);
 //           dto.content = null;
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
            if (this.uses3) {

                if (this.fichierS3Service.isObjectExist("scan/" + scan.id + ".pdf")) {
                    try {
                        this.fichierS3Service.deleteObject("scan/" + scan.id + ".pdf");
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException
                            | ErrorResponseException | InsufficientDataException | InternalException
                            | InvalidResponseException | ServerException | XmlParserException e) {
                        e.printStackTrace();
                    }
                }
            }

            scan.delete();
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
       /* if (this.uses3) {
            if (dto.isPresent()) {
                ScanDTOContent scan = dto.get();

                if (this.fichierS3Service.isObjectExist("scan/" + scan.id + ".pdf")) {
                    byte[] bytes;
                    try {
                        bytes = IOUtils.toByteArray(this.fichierS3Service.getObject("scan/" + scan.id + ".pdf"));
                        scan.content = bytes;

                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                            | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    byte[] bytes = scan.content;
                    try {
                        fichierS3Service.putObject("scan/" + scan.id + ".pdf", bytes, scan.contentContentType);
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                            | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } */
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
        if (this.uses3) {

            for (ScanDTOContent scan : dtos.content) {

                if (this.fichierS3Service.isObjectExist("scan/" + scan.id + ".pdf")) {
                    byte[] bytes;
                    try {
                        bytes = IOUtils.toByteArray(this.fichierS3Service.getObject("scan/" + scan.id + ".pdf"));
                        scan.content = bytes;

                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                            | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    byte[] bytes = scan.content;
                    try {
                        fichierS3Service.putObject("scan/" + scan.id + ".pdf", bytes, scan.contentContentType);
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                            | IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }*/
        return dtos;
    }

    public void uploadFile(MultipartFormDataInput input, long examId, boolean merge) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<String> fileNames = new ArrayList<>();
        List<InputPart> inputParts = uploadForm.get("file");
        String fileName = null;
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header =
                                                inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                if (this.hasScanFile(examId) && merge){
                   mergeFile(inputStream,"application/pdf", examId);
                } else {
                    writeFile(inputStream,"application/pdf", examId);
                }
                 } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }


    protected String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.
                getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }


    protected boolean hasScanFile(long scanId){
         String fileName = "scan/" +  scanId + ".pdf";
        return this.fichierS3Service.isObjectExist(fileName);
    }

    protected InputStream getScanFile(long scanId) throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException{
         String fileName = "scan/" +  scanId + ".pdf";
        return this.fichierS3Service.getObject(fileName);
    }

    protected void mergeFile(InputStream inputStream, String contenttype, long scanId)
            throws IOException {
                try {
                    PDFMergerUtility merger = new PDFMergerUtility();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    merger.setDestinationStream(out);;
                    merger.setDocumentMergeMode(DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);
                    RandomAccessRead r = new RandomAccessReadBuffer(inputStream);
                    RandomAccessRead l = new RandomAccessReadBuffer(this.getScanFile(scanId));
                    merger.addSource(l);
                    merger.addSource(r);
                    merger.mergeDocuments(null);
                     byte[] bytes = out.toByteArray();
        if (this.uses3){
            String fileName = "scan/" +  scanId + ".pdf";
            try {

                this.putObject(fileName, bytes,contenttype);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {
               this.updateContent(scanId, bytes);

        }

                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                    e.printStackTrace();
                }


       // Base64.Decoder encoder = Base64.getDecoder();
       // byte[] b64bytes  = encoder.decode(bytes);

    }


    protected void writeFile(InputStream inputStream, String contenttype, long scanId)
            throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);

       // Base64.Decoder encoder = Base64.getDecoder();
       // byte[] b64bytes  = encoder.decode(bytes);

        if (this.uses3){
            String fileName = "scan/" +  scanId + ".pdf";
            try {
                this.putObject(fileName, bytes,contenttype);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {
               this.updateContent(scanId, bytes);

        }
    }

    @Transactional
    protected void updateContent(long scanId, byte[] b64bytes){
        Scan s  = Scan.findById(scanId);
        s.content = b64bytes;
        s.persistOrUpdate();
    }


    protected void putObject(String name, byte[] bytes, String contenttype) throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        this.fichierS3Service.putObject(name, bytes, contenttype);
}


}
