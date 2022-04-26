package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.QuestionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Question} and its DTO {@link QuestionDTO}.
 */
@Mapper(componentModel = "cdi", uses = {ZoneMapper.class, QuestionTypeMapper.class, ExamMapper.class})
public interface QuestionMapper extends EntityMapper<QuestionDTO, Question> {

    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "type.algoName", target = "typeAlgoName")
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "exam.name", target = "examName")
    QuestionDTO toDto(Question question);

    @Mapping(source = "zoneId", target = "zone")
    @Mapping(target = "textcomments", ignore = true)
    @Mapping(target = "gradedcomments", ignore = true)
    @Mapping(source = "typeId", target = "type")
    @Mapping(source = "examId", target = "exam")
    Question toEntity(QuestionDTO questionDTO);

    default Question fromId(Long id) {
        if (id == null) {
            return null;
        }
        Question question = new Question();
        question.id = id;
        return question;
    }
}
