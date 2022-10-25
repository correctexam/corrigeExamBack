package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.GradedCommentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link GradedComment} and its DTO {@link GradedCommentDTO}.
 */
@Mapper(componentModel = "cdi", uses = {QuestionMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface GradedCommentMapper extends EntityMapper<GradedCommentDTO, GradedComment> {

    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "gradequarter", target = "grade", qualifiedByName = "gradequarter2grade")
    GradedCommentDTO toDto(GradedComment gradedComment);

    @Mapping(source = "questionId", target = "question")
    @Mapping(target = "studentResponses", ignore = true)
    @Mapping(source = "grade", target = "gradequarter", qualifiedByName = "grade2gradequarter")
    GradedComment toEntity(GradedCommentDTO gradedCommentDTO);

    default GradedComment fromId(Long id) {
        if (id == null) {
            return null;
        }
        GradedComment gradedComment = new GradedComment();
        gradedComment.id = id;
        return gradedComment;
    }

    @Named("gradequarter2grade")
    public static double quarterpoint2point(int gradequarter) {
        return Integer.valueOf(gradequarter).doubleValue() /4;
    }

    @Named("grade2gradequarter")
    public static int point2quarterpoint(double grade) {
        return Double.valueOf(grade *4).intValue();
    }
}
