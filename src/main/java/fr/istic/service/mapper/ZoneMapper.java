package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.ZoneDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Zone} and its DTO {@link ZoneDTO}.
 */
@Mapper(componentModel = "jakarta", uses = {}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ZoneMapper extends EntityMapper<ZoneDTO, Zone> {



    default Zone fromId(Long id) {
        if (id == null) {
            return null;
        }
        Zone zone = new Zone();
        zone.id = id;
        return zone;
    }
}
