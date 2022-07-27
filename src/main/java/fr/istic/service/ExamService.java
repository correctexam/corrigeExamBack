package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.StudentResponse;
import fr.istic.service.dto.ExamDTO;
import fr.istic.service.mapper.ExamMapper;
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
public class ExamService {

    private final Logger log = LoggerFactory.getLogger(ExamService.class);

    @Inject
    ExamMapper examMapper;

    @Inject
    CacheUploadService cacheService;

    @Transactional
    public ExamDTO persistOrUpdate(ExamDTO examDTO) {
        log.debug("Request to save Exam : {}", examDTO);
        var exam = examMapper.toEntity(examDTO);
        exam = Exam.persistOrUpdate(exam);
        return examMapper.toDto(exam);
    }

    /**
     * Delete the Exam by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Exam : {}", id);
        Exam.findByIdOptional(id).ifPresent(exam -> {
            StudentResponse.getAll4ExamId(id).list().forEach(sr -> sr.clearComments());
            ExamSheet.getAll4ExamId(id).list().forEach(sr -> sr.cleanBeforDelete());
            StudentResponse.getAll4ExamId(id).list().forEach(sr -> sr.delete());
            FinalResult.getAll4ExamId(id).list().forEach(f -> f.delete());
            exam.delete();
            this.cacheService.deleteFile(id);
        });
    }



    /**
     * Get one exam by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ExamDTO> findOne(Long id) {
        log.debug("Request to get Exam : {}", id);
        return Exam.findByIdOptional(id)
            .map(exam -> examMapper.toDto((Exam) exam));
    }

    /**
     * Get all the exams.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ExamDTO> findAll(Page page) {
        log.debug("Request to get all Exams");
        return new Paged<>(Exam.findAll().page(page))
            .map(exam -> examMapper.toDto((Exam) exam));
    }
    public Paged<ExamDTO> findExambyCourseId(Page page, long courseId) {
        log.debug("Request to get all Exams");
        return new Paged<>(Exam.findExambyCourseId(courseId).page(page))
            .map(exam -> examMapper.toDto((Exam) exam));
    }
    public Paged<ExamDTO> findExambyScanId(Page page, long scanId) {
        log.debug("Request to get all Exams by scanId");
        return new Paged<>(Exam.findExambyScanId(scanId).page(page))
            .map(exam -> examMapper.toDto((Exam) exam));
    }



}
