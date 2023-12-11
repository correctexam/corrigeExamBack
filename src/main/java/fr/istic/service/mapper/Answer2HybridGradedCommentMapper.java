package fr.istic.service.mapper;

import fr.istic.domain.*;
import fr.istic.service.dto.Answer2HybridGradedCommentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Answer2HybridGradedComment} and its DTO {@link Answer2HybridGradedCommentDTO}.
 */
@Mapper(componentModel = "cdi", uses = { HybridGradedCommentMapper.class, StudentResponseMapper.class })
public interface Answer2HybridGradedCommentMapper extends EntityMapper<Answer2HybridGradedCommentDTO, Answer2HybridGradedComment> {
    @Mapping(source = "hybridcomments.id", target = "hybridcommentsId")
    @Mapping(source = "hybridcomments.text", target = "hybridcommentsText")
    @Mapping(source = "studentResponse.id", target = "studentResponseId")
    Answer2HybridGradedCommentDTO toDto(Answer2HybridGradedComment answer2HybridGradedComment);

    @Mapping(source = "hybridcommentsId", target = "hybridcomments")
    @Mapping(source = "studentResponseId", target = "studentResponse")
    Answer2HybridGradedComment toEntity(Answer2HybridGradedCommentDTO answer2HybridGradedCommentDTO);

    default Answer2HybridGradedComment fromId(Long id) {
        if (id == null) {
            return null;
        }
        Answer2HybridGradedComment answer2HybridGradedComment = new Answer2HybridGradedComment();
        answer2HybridGradedComment.id = id;
        return answer2HybridGradedComment;
    }
}
