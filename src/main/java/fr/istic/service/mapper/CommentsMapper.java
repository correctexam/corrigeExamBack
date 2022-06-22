package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.CommentsDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Comments} and its DTO {@link CommentsDTO}.
 */
@Mapper(componentModel = "cdi", uses = {StudentResponseMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CommentsMapper extends EntityMapper<CommentsDTO, Comments> {

    @Mapping(source = "studentResponse.id", target = "studentResponseId")
    CommentsDTO toDto(Comments comments);

    @Mapping(source = "studentResponseId", target = "studentResponse")
    Comments toEntity(CommentsDTO commentsDTO);

    default Comments fromId(Long id) {
        if (id == null) {
            return null;
        }
        Comments comments = new Comments();
        comments.id = id;
        return comments;
    }
}
