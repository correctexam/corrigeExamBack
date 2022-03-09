package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.QuestionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Question} and its DTO {@link QuestionDTO}.
 */
@Mapper(componentModel = "cdi", uses = {ZoneMapper.class, ExamMapper.class})
public interface QuestionMapper extends EntityMapper<QuestionDTO, Question> {

    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "exam.name", target = "examName")
    QuestionDTO toDto(Question question);

    @Mapping(source = "zoneId", target = "zone")
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
