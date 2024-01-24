package fr.istic.service.mapper;

import fr.istic.domain.*;
import fr.istic.service.dto.HybridGradedCommentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link HybridGradedComment} and its DTO {@link HybridGradedCommentDTO}.
 */
@Mapper(componentModel = "jakarta", uses = {QuestionMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface HybridGradedCommentMapper extends EntityMapper<HybridGradedCommentDTO, HybridGradedComment> {
    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "grade", target = "grade", qualifiedByName = "quarterpoint2point")
    HybridGradedCommentDTO toDto(HybridGradedComment hybridGradedComment);

    @Mapping(source = "questionId", target = "question")
    @Mapping(target = "valueAnswers", ignore = true)
    @Mapping(source = "grade", target = "grade", qualifiedByName = "point2quarterpoint")

    HybridGradedComment toEntity(HybridGradedCommentDTO hybridGradedCommentDTO);

    default HybridGradedComment fromId(Long id) {
        if (id == null) {
            return null;
        }
        HybridGradedComment hybridGradedComment = new HybridGradedComment();
        hybridGradedComment.id = id;
        return hybridGradedComment;
    }

    @Named("quarterpoint2point")
    public static Double quarterpoint2point(Integer quarterpoint) {
        return Integer.valueOf(quarterpoint).doubleValue() /4;
    }

    @Named("point2quarterpoint")
    public static Integer point2quarterpoint(Double point) {
        return Double.valueOf(point *4).intValue();
    }
}
