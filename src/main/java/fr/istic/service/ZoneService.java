package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Zone;
import fr.istic.service.dto.ZoneDTO;
import fr.istic.service.mapper.ZoneMapper;
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
public class ZoneService {

    private final Logger log = LoggerFactory.getLogger(ZoneService.class);

    @Inject
    ZoneMapper zoneMapper;

    @Transactional
    public ZoneDTO persistOrUpdate(ZoneDTO zoneDTO) {
        log.debug("Request to save Zone : {}", zoneDTO);
        var zone = zoneMapper.toEntity(zoneDTO);
        zone = Zone.persistOrUpdate(zone);
        return zoneMapper.toDto(zone);
    }

    /**
     * Delete the Zone by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Zone : {}", id);
        Zone.findByIdOptional(id).ifPresent(zone -> {
            zone.delete();
        });
    }

    /**
     * Get one zone by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ZoneDTO> findOne(Long id) {
        log.debug("Request to get Zone : {}", id);
        return Zone.findByIdOptional(id)
            .map(zone -> zoneMapper.toDto((Zone) zone)); 
    }

    /**
     * Get all the zones.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ZoneDTO> findAll(Page page) {
        log.debug("Request to get all Zones");
        return new Paged<>(Zone.findAll().page(page))
            .map(zone -> zoneMapper.toDto((Zone) zone));
    }



}
