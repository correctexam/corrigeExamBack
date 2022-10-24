package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.QuestionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Question} and its DTO {@link QuestionDTO}.
 */
@Mapper(componentModel = "cdi", uses = {ZoneMapper.class, QuestionTypeMapper.class, ExamMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface QuestionMapper extends EntityMapper<QuestionDTO, Question> {

    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "type.algoName", target = "typeAlgoName")
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "exam.name", target = "examName")
    @Mapping(source = "quarterpoint", target = "point", qualifiedByName = "quarterpoint2point")

    QuestionDTO toDto(Question question);

    @Mapping(source = "zoneId", target = "zone")
    @Mapping(target = "textcomments", ignore = true)
    @Mapping(target = "gradedcomments", ignore = true)
    @Mapping(source = "typeId", target = "type")
    @Mapping(source = "examId", target = "exam")
    @Mapping(source = "point", target = "quarterpoint", qualifiedByName = "point2quarterpoint")
    Question toEntity(QuestionDTO questionDTO);

    default Question fromId(Long id) {
        if (id == null) {
            return null;
        }
        Question question = new Question();
        question.id = id;
        return question;
    }

    @Named("quarterpoint2point")
    public static double quarterpoint2point(int quarterpoint) {
        return Integer.valueOf(quarterpoint).doubleValue() /4;
    }

    @Named("point2quarterpoint")
    public static int point2quarterpoint(double point) {
        return Double.valueOf(point *4).intValue();
    }
}
