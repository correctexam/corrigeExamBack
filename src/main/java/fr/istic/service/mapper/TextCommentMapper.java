package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.TextCommentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link TextComment} and its DTO {@link TextCommentDTO}.
 */
@Mapper(componentModel = "cdi", uses = {QuestionMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TextCommentMapper extends EntityMapper<TextCommentDTO, TextComment> {

    @Mapping(source = "question.id", target = "questionId")
    TextCommentDTO toDto(TextComment textComment);

    @Mapping(source = "questionId", target = "question")
    @Mapping(target = "studentResponses", ignore = true)
    TextComment toEntity(TextCommentDTO textCommentDTO);

    default TextComment fromId(Long id) {
        if (id == null) {
            return null;
        }
        TextComment textComment = new TextComment();
        textComment.id = id;
        return textComment;
    }
}
