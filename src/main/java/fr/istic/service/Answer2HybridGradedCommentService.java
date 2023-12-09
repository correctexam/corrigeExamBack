package fr.istic.service;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.service.dto.Answer2HybridGradedCommentDTO;
import fr.istic.service.mapper.Answer2HybridGradedCommentMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Transactional
public class Answer2HybridGradedCommentService {

    private final Logger log = LoggerFactory.getLogger(Answer2HybridGradedCommentService.class);

    @Inject
    Answer2HybridGradedCommentMapper answer2HybridGradedCommentMapper;

    @Transactional
    public Answer2HybridGradedCommentDTO persistOrUpdate(Answer2HybridGradedCommentDTO answer2HybridGradedCommentDTO) {
        log.debug("Request to save Answer2HybridGradedComment : {}", answer2HybridGradedCommentDTO);
        var answer2HybridGradedComment = answer2HybridGradedCommentMapper.toEntity(answer2HybridGradedCommentDTO);
        answer2HybridGradedComment = Answer2HybridGradedComment.persistOrUpdate(answer2HybridGradedComment);
        return answer2HybridGradedCommentMapper.toDto(answer2HybridGradedComment);
    }

    /**
     * Delete the Answer2HybridGradedComment by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Answer2HybridGradedComment : {}", id);
        Answer2HybridGradedComment
            .findByIdOptional(id)
            .ifPresent(answer2HybridGradedComment -> {
                answer2HybridGradedComment.delete();
            });
    }

    /**
     * Get one answer2HybridGradedComment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<Answer2HybridGradedCommentDTO> findOne(Long id) {
        log.debug("Request to get Answer2HybridGradedComment : {}", id);
        return Answer2HybridGradedComment
            .findByIdOptional(id)
            .map(answer2HybridGradedComment ->
                answer2HybridGradedCommentMapper.toDto((Answer2HybridGradedComment) answer2HybridGradedComment)
            );
    }

    /**
     * Get all the answer2HybridGradedComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<Answer2HybridGradedCommentDTO> findAll(Page page) {
        log.debug("Request to get all Answer2HybridGradedComments");
        return new Paged<>(Answer2HybridGradedComment.findAll().page(page))
            .map(answer2HybridGradedComment ->
                answer2HybridGradedCommentMapper.toDto((Answer2HybridGradedComment) answer2HybridGradedComment)
            );
    }


        /**
     * Get all the answer2HybridGradedComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Answer2HybridGradedCommentDTO incrementWithResponseIdAndHybridCommentId(long responseId, long hybridCommentId) {
        log.debug("Request to incrementWithResponseIdAndHybridCommentId");
        var q = Answer2HybridGradedComment.findAllWithResponseIdAndHybridCommentId(responseId, hybridCommentId);
        if (q.count() == 0){
            Answer2HybridGradedCommentDTO dto = new Answer2HybridGradedCommentDTO();
            dto.stepValue = 1;
            dto.studentResponseId= responseId;
            dto.hybridcommentsId = hybridCommentId;
            return this.persistOrUpdate(dto);
        } else {
             Answer2HybridGradedComment o = q.firstResult();
            o.stepValue = o.stepValue+1;
            if (o.stepValue> o.hybridcomments.step){
                o.stepValue = 0;
            }
            o.persistOrUpdate();
            return answer2HybridGradedCommentMapper.toDto(o);
        }
    }


        /**
     * Get all the answer2HybridGradedComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<Answer2HybridGradedCommentDTO> findAllAnswerHybridGradedCommentByAnswerId(Page page,long answerId) {
        log.debug("Request to get all Answer2HybridGradedComments by answwer id");
        var res = Answer2HybridGradedComment.findAllAnswerHybridGradedCommentByAnswerId(answerId);
        return new Paged<>(res.page(page))
            .map(answer2HybridGradedComment ->
                answer2HybridGradedCommentMapper.toDto((Answer2HybridGradedComment) answer2HybridGradedComment)
            );
    }
    public void deleteAllAnswerHybridGradedCommentByCommentId(long  commentId){
        Answer2HybridGradedComment.deleteAllAnswerHybridGradedCommentByCommentId(commentId);
    }
    public void deleteAllAnswerHybridGradedCommentByAnswerId(long  answerId){
        Answer2HybridGradedComment.deleteAllAnswerHybridGradedCommentByAnswerId(answerId);
    }


}
