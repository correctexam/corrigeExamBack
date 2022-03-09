package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.ScanDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Scan} and its DTO {@link ScanDTO}.
 */
@Mapper(componentModel = "cdi", uses = {})
public interface ScanMapper extends EntityMapper<ScanDTO, Scan> {


    @Mapping(target = "sheets", ignore = true)
    Scan toEntity(ScanDTO scanDTO);

    default Scan fromId(Long id) {
        if (id == null) {
            return null;
        }
        Scan scan = new Scan();
        scan.id = id;
        return scan;
    }
}
