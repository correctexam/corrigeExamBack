package fr.istic.service;

import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.service.dto.HybridGradedCommentDTO;
import fr.istic.service.mapper.HybridGradedCommentMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Transactional
public class HybridGradedCommentService {

    private final Logger log = LoggerFactory.getLogger(HybridGradedCommentService.class);

    @Inject
    HybridGradedCommentMapper hybridGradedCommentMapper;

    @Inject
    Answer2HybridGradedCommentService answer2HybridGradedCommentService;

    @Transactional
    public HybridGradedCommentDTO persistOrUpdate(HybridGradedCommentDTO hybridGradedCommentDTO) {
        log.debug("Request to save HybridGradedComment : {}", hybridGradedCommentDTO);
        var hybridGradedComment = hybridGradedCommentMapper.toEntity(hybridGradedCommentDTO);
        hybridGradedComment = HybridGradedComment.persistOrUpdate(hybridGradedComment);
        return hybridGradedCommentMapper.toDto(hybridGradedComment);
    }

    /**
     * Delete the HybridGradedComment by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete HybridGradedComment : {}", id);
        this.answer2HybridGradedCommentService.deleteAllAnswerHybridGradedCommentByCommentId(id);
        HybridGradedComment
            .findByIdOptional(id)
            .ifPresent(hybridGradedComment -> {
                hybridGradedComment.delete();
            });
    }

    /**
     * Get one hybridGradedComment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<HybridGradedCommentDTO> findOne(Long id) {
        log.debug("Request to get HybridGradedComment : {}", id);
        return HybridGradedComment
            .findByIdOptional(id)
            .map(hybridGradedComment -> hybridGradedCommentMapper.toDto((HybridGradedComment) hybridGradedComment));
    }

    /**
     * Get all the hybridGradedComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<HybridGradedCommentDTO> findAll(Page page) {
        log.debug("Request to get all HybridGradedComments");
        return new Paged<>(HybridGradedComment.findAll().page(page))
            .map(hybridGradedComment -> hybridGradedCommentMapper.toDto((HybridGradedComment) hybridGradedComment));
    }

    public Paged<HybridGradedCommentDTO> findHybridGradedCommentByQuestionId(Page page, long qId) {
         log.debug("Request to get all GradedComments by QID");
        return new Paged<>(HybridGradedComment.findByQuestionId(qId).page(page))
            .map(hgradedComment -> hybridGradedCommentMapper.toDto((HybridGradedComment) hgradedComment));    }
}
