package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.QuestionType;
import fr.istic.service.dto.QuestionTypeDTO;
import fr.istic.service.mapper.QuestionTypeMapper;
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
public class QuestionTypeService {

    private final Logger log = LoggerFactory.getLogger(QuestionTypeService.class);

    @Inject
    QuestionTypeMapper questionTypeMapper;

    @Transactional
    public QuestionTypeDTO persistOrUpdate(QuestionTypeDTO questionTypeDTO) {
        log.debug("Request to save QuestionType : {}", questionTypeDTO);
        var questionType = questionTypeMapper.toEntity(questionTypeDTO);
        questionType = QuestionType.persistOrUpdate(questionType);
        return questionTypeMapper.toDto(questionType);
    }

    /**
     * Delete the QuestionType by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete QuestionType : {}", id);
        QuestionType.findByIdOptional(id).ifPresent(questionType -> {
            questionType.delete();
        });
    }

    /**
     * Get one questionType by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<QuestionTypeDTO> findOne(Long id) {
        log.debug("Request to get QuestionType : {}", id);
        return QuestionType.findByIdOptional(id)
            .map(questionType -> questionTypeMapper.toDto((QuestionType) questionType)); 
    }

    /**
     * Get all the questionTypes.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<QuestionTypeDTO> findAll(Page page) {
        log.debug("Request to get all QuestionTypes");
        return new Paged<>(QuestionType.findAll().page(page))
            .map(questionType -> questionTypeMapper.toDto((QuestionType) questionType));
    }



}
