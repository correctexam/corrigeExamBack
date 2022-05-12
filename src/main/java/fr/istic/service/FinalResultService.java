package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.FinalResult;
import fr.istic.service.dto.FinalResultDTO;
import fr.istic.service.mapper.FinalResultMapper;
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
public class FinalResultService {

    private final Logger log = LoggerFactory.getLogger(FinalResultService.class);

    @Inject
    FinalResultMapper finalResultMapper;

    @Transactional
    public FinalResultDTO persistOrUpdate(FinalResultDTO finalResultDTO) {
        log.debug("Request to save FinalResult : {}", finalResultDTO);
        var finalResult = finalResultMapper.toEntity(finalResultDTO);
        finalResult = FinalResult.persistOrUpdate(finalResult);
        return finalResultMapper.toDto(finalResult);
    }

    /**
     * Delete the FinalResult by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete FinalResult : {}", id);
        FinalResult.findByIdOptional(id).ifPresent(finalResult -> {
            finalResult.delete();
        });
    }

    /**
     * Get one finalResult by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<FinalResultDTO> findOne(Long id) {
        log.debug("Request to get FinalResult : {}", id);
        return FinalResult.findByIdOptional(id)
            .map(finalResult -> finalResultMapper.toDto((FinalResult) finalResult));
    }

    /**
     * Get all the finalResults.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<FinalResultDTO> findAll(Page page) {
        log.debug("Request to get all FinalResults");
        return new Paged<>(FinalResult.findAll().page(page))
            .map(finalResult -> finalResultMapper.toDto((FinalResult) finalResult));
    }


        /**
     * Get all the finalResults.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<FinalResultDTO> findFinalResultbyExamIdAndStudentId(Page page, long examId, long  studentId) {
        log.debug("Request to get all FinalResults");
        return new Paged<>(FinalResult.findFinalResultByStudentIdAndExamId(studentId,examId).page(page))
            .map(finalResult -> finalResultMapper.toDto((FinalResult) finalResult));
    }





}
