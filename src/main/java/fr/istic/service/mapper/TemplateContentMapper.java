package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.TemplateDTOContent;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Template} and its DTO {@link TemplateDTOContent}.
 */
@Mapper(componentModel = "jakarta", uses = {}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TemplateContentMapper extends EntityMapper<TemplateDTOContent, Template> {


    @Mapping(target = "exam", ignore = true)
    Template toEntity(TemplateDTOContent templateDTO);

    default Template fromId(Long id) {
        if (id == null) {
            return null;
        }
        Template template = new Template();
        template.id = id;
        return template;
    }
}
