package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.TextComment;
import fr.istic.service.dto.TextCommentDTO;
import fr.istic.service.mapper.TextCommentMapper;
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
public class TextCommentService {

    private final Logger log = LoggerFactory.getLogger(TextCommentService.class);

    @Inject
    TextCommentMapper textCommentMapper;

    @Transactional
    public TextCommentDTO persistOrUpdate(TextCommentDTO textCommentDTO) {
        log.debug("Request to save TextComment : {}", textCommentDTO);
        var textComment = textCommentMapper.toEntity(textCommentDTO);
        textComment = TextComment.persistOrUpdate(textComment);
        return textCommentMapper.toDto(textComment);
    }

    /**
     * Delete the TextComment by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete TextComment : {}", id);
        TextComment.findByIdOptional(id).ifPresent(textComment -> {
            textComment.delete();
        });
    }

    /**
     * Get one textComment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<TextCommentDTO> findOne(Long id) {
        log.debug("Request to get TextComment : {}", id);
        return TextComment.findByIdOptional(id)
            .map(textComment -> textCommentMapper.toDto((TextComment) textComment)); 
    }

    /**
     * Get all the textComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<TextCommentDTO> findAll(Page page) {
        log.debug("Request to get all TextComments");
        return new Paged<>(TextComment.findAll().page(page))
            .map(textComment -> textCommentMapper.toDto((TextComment) textComment));
    }



}
