package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Scan;
import fr.istic.service.dto.ScanDTO;
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

    @Transactional
    public ScanDTO persistOrUpdate(ScanDTO scanDTO) {
        log.debug("Request to save Scan : {}", scanDTO);
        var scan = scanMapper.toEntity(scanDTO);
        scan = Scan.persistOrUpdate(scan);
        return scanMapper.toDto(scan);
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
    public Optional<ScanDTO> findOne(Long id) {
        log.debug("Request to get Scan : {}", id);
        return Scan.findByIdOptional(id)
            .map(scan -> scanMapper.toDto((Scan) scan)); 
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



}
