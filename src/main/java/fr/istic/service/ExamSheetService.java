package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.ExamSheet;
import fr.istic.service.dto.ExamSheetDTO;
import fr.istic.service.mapper.ExamSheetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class ExamSheetService {

    private final Logger log = LoggerFactory.getLogger(ExamSheetService.class);

    @Inject
    ExamSheetMapper examSheetMapper;

    @Transactional
    public ExamSheetDTO persistOrUpdate(ExamSheetDTO examSheetDTO) {
        log.debug("Request to save ExamSheet : {}", examSheetDTO);
        var examSheet = examSheetMapper.toEntity(examSheetDTO);
        examSheet = ExamSheet.persistOrUpdate(examSheet);
        return examSheetMapper.toDto(examSheet);
    }

    /**
     * Delete the ExamSheet by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete ExamSheet : {}", id);
        ExamSheet.findByIdOptional(id).ifPresent(examSheet -> {
            examSheet.delete();
        });
    }

    /**
     * Get one examSheet by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ExamSheetDTO> findOne(Long id) {
        log.debug("Request to get ExamSheet : {}", id);
        return ExamSheet.findByIdOptional(id)
            .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }

    /**
     * Get all the examSheets.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ExamSheetDTO> findAll(Page page) {
        log.debug("Request to get all ExamSheets");
        return new Paged<>(ExamSheet.findAll().page(page))
            .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }

      /**
     * Get all the examSheets.
     * @param page the pagination information.
     * @return the list of entities by name.
     */
    public Paged<ExamSheetDTO> findExamSheetByName(Page page, String name) {
        log.debug("Request to get all ExamSheets by name");
        return new Paged<>(ExamSheet.findExamSheetByName(name).page(page))
            .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }


}
