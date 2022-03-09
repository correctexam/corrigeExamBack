package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Comments;
import fr.istic.service.dto.CommentsDTO;
import fr.istic.service.mapper.CommentsMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class CommentsService {

    private final Logger log = LoggerFactory.getLogger(CommentsService.class);

    @Inject
    CommentsMapper commentsMapper;

    @Transactional
    public CommentsDTO persistOrUpdate(CommentsDTO commentsDTO) {
        log.debug("Request to save Comments : {}", commentsDTO);
        var comments = commentsMapper.toEntity(commentsDTO);
        comments = Comments.persistOrUpdate(comments);
        return commentsMapper.toDto(comments);
    }

    /**
     * Delete the Comments by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Comments : {}", id);
        Comments.findByIdOptional(id).ifPresent(comments -> {
            comments.delete();
        });
    }

    /**
     * Get one comments by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<CommentsDTO> findOne(Long id) {
        log.debug("Request to get Comments : {}", id);
        return Comments.findByIdOptional(id)
            .map(comments -> commentsMapper.toDto((Comments) comments)); 
    }

    /**
     * Get all the comments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CommentsDTO> findAll(Page page) {
        log.debug("Request to get all Comments");
        return new Paged<>(Comments.findAll().page(page))
            .map(comments -> commentsMapper.toDto((Comments) comments));
    }



}
