package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.ScanDTOContent;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Scan} and its DTO {@link ScanDTOContent}.
 */
@Mapper(componentModel = "cdi", uses = {}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ScanContentMapper extends EntityMapper<ScanDTOContent, Scan> {


    @Mapping(target = "sheets", ignore = true)
    Scan toEntity(ScanDTOContent scanDTO);

    default Scan fromId(Long id) {
        if (id == null) {
            return null;
        }
        Scan scan = new Scan();
        scan.id = id;
        return scan;
    }
}
