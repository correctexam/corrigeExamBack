package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.TemplateDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Template} and its DTO {@link TemplateDTO}.
 */
@Mapper(componentModel = "cdi", uses = {})
public interface TemplateMapper extends EntityMapper<TemplateDTO, Template> {


    @Mapping(target = "exam", ignore = true)
    Template toEntity(TemplateDTO templateDTO);

    default Template fromId(Long id) {
        if (id == null) {
            return null;
        }
        Template template = new Template();
        template.id = id;
        return template;
    }
}
