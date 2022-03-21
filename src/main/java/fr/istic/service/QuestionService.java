package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Question;
import fr.istic.service.dto.QuestionDTO;
import fr.istic.service.mapper.QuestionMapper;
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
public class QuestionService {

    private final Logger log = LoggerFactory.getLogger(QuestionService.class);

    @Inject
    QuestionMapper questionMapper;

    @Transactional
    public QuestionDTO persistOrUpdate(QuestionDTO questionDTO) {
        log.debug("Request to save Question : {}", questionDTO);
        var question = questionMapper.toEntity(questionDTO);
        question = Question.persistOrUpdate(question);
        return questionMapper.toDto(question);
    }

    /**
     * Delete the Question by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Question : {}", id);
        Question.findByIdOptional(id).ifPresent(question -> {
            question.delete();
        });
    }

    /**
     * Get one question by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<QuestionDTO> findOne(Long id) {
        log.debug("Request to get Question : {}", id);
        return Question.findByIdOptional(id)
            .map(question -> questionMapper.toDto((Question) question));
    }

    /**
     * Get all the questions.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<QuestionDTO> findAll(Page page) {
        log.debug("Request to get all Questions");
        return new Paged<>(Question.findAll().page(page))
            .map(question -> questionMapper.toDto((Question) question));
    }

    public Paged<QuestionDTO> findQuestionbyExamId(Page page, long examId) {
        log.debug("Request to get Questions by ExamID");
        return new Paged<>(Question.findQuestionbyExamId(examId).page(page))
            .map(question -> questionMapper.toDto((Question) question));
    }
    public Paged<QuestionDTO> findQuestionbyZoneId(Page page, long zoneId) {
        log.debug("Request to get Questions by ZoneId");
        return new Paged<>(Question.findQuestionbyZoneId(zoneId).page(page))
            .map(question -> questionMapper.toDto((Question) question));
    }



}
