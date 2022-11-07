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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

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
    public ScanDTOContent persistOrUpdate(ScanDTOContent scanDTO) {
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
            byte[] bytes = scanDTO.content;
            scan.content = null;
            scan = Scan.persistOrUpdate(scan);
            try {
                fichierS3Service.putObject("scan/" + scan.id + ".pdf", bytes, scanDTO.contentContentType);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
                e.printStackTrace();
            }
            ScanDTOContent dto = scanContentMapper.toDto(scan);
            dto.content = bytes;
            return dto;
        } else {
            scan = Scan.persistOrUpdate(scan);
            ScanDTOContent dto = scanContentMapper.toDto(scan);
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
    public Optional<ScanDTOContent> findOne(Long id) {
        log.debug("Request to get Scan : {}", id);
        Optional<Scan> scanop = Scan.findByIdOptional(id);
        if (this.uses3) {
            if (scanop.isPresent()) {
                Scan scan = scanop.get();

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
        }
        return scanop
                .map(scan -> scanContentMapper.toDto((Scan) scan));
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
    public Paged<ScanDTOContent> findbyName(String name, Page page) {
        log.debug("Request to get all Scans by name");
        Paged<Scan> scans = new Paged<>(Scan.findByName(name).page(page));
        if (this.uses3) {

            for (Scan scan : scans.content) {

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
        }
        return scans.map(scan -> scanContentMapper.toDto((Scan) scan));
    }

}
