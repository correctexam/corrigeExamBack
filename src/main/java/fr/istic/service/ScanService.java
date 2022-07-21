package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Scan;
import fr.istic.service.dto.ScanDTO;
import fr.istic.service.dto.ScanDTOContent;
import fr.istic.service.mapper.ScanContentMapper;
import fr.istic.service.mapper.ScanMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ScanService {

    private final Logger log = LoggerFactory.getLogger(ScanService.class);

    @Inject
    ScanMapper scanMapper;

    @Inject
    ScanContentMapper scanContentMapper;


    @Transactional
    public ScanDTOContent persistOrUpdate(ScanDTOContent scanDTO) {
        log.debug("Request to save Scan : {}", scanDTO);
        var scan = scanContentMapper.toEntity(scanDTO);
        if (scanDTO.name.endsWith("indexdb.json")){
            PanacheQuery<Scan> q = Scan.findByName(scanDTO.name);
            long number = q.count();
            if (number > 0){
                Scan s = q.firstResult();
                s.content = scan.content;
                scan = Scan.persistOrUpdate(s);
                return scanContentMapper.toDto(s);
            } else {
                scan = Scan.persistOrUpdate(scan);
                return scanContentMapper.toDto(scan);
            }

        } else {
            scan = Scan.persistOrUpdate(scan);
            return scanContentMapper.toDto(scan);

        }

    }

    /**
     * Delete the Scan by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Scan : {}", id);
        Scan.findByIdOptional(id).ifPresent(scan -> {
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
        return Scan.findByIdOptional(id)
            .map(scan -> scanContentMapper.toDto((Scan) scan));
    }

    /**
     * Get all the scans.
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
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ScanDTOContent> findbyName(String name, Page page) {
        log.debug("Request to get all Scans by name");
        return new Paged<>(Scan.findByName(name).page(page))
            .map(scan -> scanContentMapper.toDto((Scan) scan));
    }


}
