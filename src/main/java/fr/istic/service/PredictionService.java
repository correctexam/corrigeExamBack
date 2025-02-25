package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Prediction;
import fr.istic.service.dto.PredictionDTO;
import fr.istic.service.mapper.PredictionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class PredictionService {

    private final Logger log = LoggerFactory.getLogger(PredictionService.class);

    @Inject
    PredictionMapper predictionMapper;

    /**
     * Persist or update a prediction entity.
     *
     * @param predictionDTO the DTO of the entity.
     * @return the persisted or updated DTO.
     */
    @Transactional
    public PredictionDTO persistOrUpdate(PredictionDTO predictionDTO) {
        log.debug("Request to save Prediction : {}", predictionDTO);
        var prediction = predictionMapper.toEntity(predictionDTO);
        prediction = Prediction.persistOrUpdate(prediction);
        return predictionMapper.toDto(prediction);
    }

    /**
     * Delete the Prediction by ID.
     *
     * @param id the ID of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Prediction : {}", id);
        Prediction.findByIdOptional(id).ifPresent(prediction -> {
            // Delete the prediction entity
            prediction.delete();
        });
    }


        /**
     * Delete the Prediction by ID.
     *
     * @param id the ID of the entity.
     */
    @Transactional
    public void deleteByQuestionId(Long questionId) {
        log.debug("Request to delete Prediction for question  {}", questionId);
        Prediction.deleteByQId(questionId);
    }



    /**
     * Get one Prediction by ID.
     *
     * @param id the ID of the entity.
     * @return the entity as an optional DTO.
     */
    public Optional<PredictionDTO> findOne(Long id) {
        log.debug("Request to get Prediction : {}", id);
        return Prediction.findByIdOptional(id)
            .map(prediction -> predictionMapper.toDto((Prediction) prediction));
    }

    /**
     * Get all the Predictions.
     *
     * @param page the pagination information.
     * @return a paged list of entities.
     */
    public Paged<PredictionDTO> findAll(Page page) {
        log.debug("Request to get all Predictions");
        return new Paged<>(Prediction.findAll().page(page))
            .map(prediction -> predictionMapper.toDto((Prediction) prediction));
    }

    /**
     * Get all the Predictions by Question ID.
     *
     * @param page the pagination information.
     * @param questionId the ID of the related question.
     * @return a paged list of entities.
     */
    public Paged<PredictionDTO> findPredictionByQuestionId(Page page, long questionId) {
        log.debug("Request to get all Predictions by Question ID");
        return new Paged<>(Prediction.findByQuestionId(questionId).page(page))
            .map(prediction -> predictionMapper.toDto((Prediction) prediction));
    }
}
