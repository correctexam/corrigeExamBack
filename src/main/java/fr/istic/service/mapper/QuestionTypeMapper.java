package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.QuestionTypeDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link QuestionType} and its DTO {@link QuestionTypeDTO}.
 */
@Mapper(componentModel = "cdi", uses = {})
public interface QuestionTypeMapper extends EntityMapper<QuestionTypeDTO, QuestionType> {



    default QuestionType fromId(Long id) {
        if (id == null) {
            return null;
        }
        QuestionType questionType = new QuestionType();
        questionType.id = id;
        return questionType;
    }
}
