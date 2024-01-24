package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.GradedComment;
import fr.istic.domain.StudentResponse;
import fr.istic.service.dto.GradedCommentDTO;
import fr.istic.service.mapper.GradedCommentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class GradedCommentService {

    private final Logger log = LoggerFactory.getLogger(GradedCommentService.class);

    @Inject
    GradedCommentMapper gradedCommentMapper;

    @Transactional
    public GradedCommentDTO persistOrUpdate(GradedCommentDTO gradedCommentDTO) {
        log.debug("Request to save GradedComment : {}", gradedCommentDTO);
        var gradedComment = gradedCommentMapper.toEntity(gradedCommentDTO);
        gradedComment = GradedComment.persistOrUpdate(gradedComment);
        return gradedCommentMapper.toDto(gradedComment);
    }

    /**
     * Delete the GradedComment by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete GradedComment : {}", id);
        GradedComment.findByIdOptional(id).ifPresent(gradedComment -> {
                List<StudentResponse> srs = StudentResponse.findAllByGradedCommentsIds(id).list();
                srs.forEach(e-> {
                    e.gradedcomments.remove(gradedComment);
                    StudentResponse.update(e);
                });
            gradedComment.delete();
        });
    }

    /**
     * Get one gradedComment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<GradedCommentDTO> findOne(Long id) {
        log.debug("Request to get GradedComment : {}", id);
        return GradedComment.findByIdOptional(id)
            .map(gradedComment -> gradedCommentMapper.toDto((GradedComment) gradedComment));
    }

    /**
     * Get all the gradedComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<GradedCommentDTO> findAll(Page page) {
        log.debug("Request to get all GradedComments");
        return new Paged<>(GradedComment.findAll().page(page))
            .map(gradedComment -> gradedCommentMapper.toDto((GradedComment) gradedComment));
    }
    /**
     * Get all the textComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<GradedCommentDTO> findGradedCommentByQuestionId(Page page, long qId) {
        log.debug("Request to get all GradedComments by QID");
        return new Paged<>(GradedComment.findByQuestionId(qId).page(page))
            .map(gradedComment -> gradedCommentMapper.toDto((GradedComment) gradedComment));
    }



}
