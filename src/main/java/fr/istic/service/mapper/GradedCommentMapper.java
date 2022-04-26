package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.GradedCommentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link GradedComment} and its DTO {@link GradedCommentDTO}.
 */
@Mapper(componentModel = "cdi", uses = {QuestionMapper.class})
public interface GradedCommentMapper extends EntityMapper<GradedCommentDTO, GradedComment> {

    @Mapping(source = "question.id", target = "questionId")
    GradedCommentDTO toDto(GradedComment gradedComment);

    @Mapping(source = "questionId", target = "question")
    @Mapping(target = "studentResponses", ignore = true)
    GradedComment toEntity(GradedCommentDTO gradedCommentDTO);

    default GradedComment fromId(Long id) {
        if (id == null) {
            return null;
        }
        GradedComment gradedComment = new GradedComment();
        gradedComment.id = id;
        return gradedComment;
    }
}
