package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.StudentResponse;
import fr.istic.service.customdto.StudentResponseNote;
import fr.istic.service.dto.StudentResponseDTO;
import fr.istic.service.mapper.StudentResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class StudentResponseService {

    private final Logger log = LoggerFactory.getLogger(StudentResponseService.class);

    @Inject
    StudentResponseMapper studentResponseMapper;

    @Inject
    Answer2HybridGradedCommentService answer2HybridGradedCommentService;

    @Transactional
    public StudentResponseDTO persistOrUpdate(StudentResponseDTO studentResponseDTO) {
        log.debug("Request to save StudentResponse : {}", studentResponseDTO);
        long l = 0;
        if (studentResponseDTO.sheetId != null && studentResponseDTO.questionId != null) {
            l = StudentResponse.findStudentResponsesbysheetIdAndquestionId(studentResponseDTO.sheetId,Collections.singletonList( studentResponseDTO.questionId) ).count();

        }
        if (l>0) {
            StudentResponse sr = StudentResponse.findStudentResponsesbysheetIdAndquestionId(studentResponseDTO.sheetId,Collections.singletonList(studentResponseDTO.questionId)).firstResult();
            var studentResponse = studentResponseMapper.toEntity(studentResponseDTO);
            sr.quarternote = studentResponse.quarternote;
            sr.star = studentResponse.star;
            sr.worststar = studentResponse.worststar;
            sr.comments.clear();
            sr.comments.addAll(studentResponse.comments);
            sr.gradedcomments.clear();
            sr.gradedcomments.addAll(studentResponse.gradedcomments);
            sr.textcomments.clear();
            sr.textcomments.addAll(studentResponse.textcomments);
            studentResponse = StudentResponse.persistOrUpdate(sr);
            return studentResponseMapper.toDto(sr);

        } else{
            var studentResponse = studentResponseMapper.toEntity(studentResponseDTO);

            studentResponse = StudentResponse.persistOrUpdate(studentResponse);
            return studentResponseMapper.toDto(studentResponse);

        }

    }

    /**
     * Delete the StudentResponse by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete StudentResponse : {}", id);

        StudentResponse.findByIdOptional(id).ifPresent(studentResponse -> {
            this.answer2HybridGradedCommentService.deleteAllAnswerHybridGradedCommentByAnswerId(id);
            studentResponse.delete();
        });
    }

    /**
     * Get one studentResponse by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<StudentResponseDTO> findOne(Long id) {
        log.debug("Request to get StudentResponse : {}", id);
        return StudentResponse.findOneWithEagerRelationships(id)
            .map(studentResponse -> studentResponseMapper.toDto((StudentResponse) studentResponse));
    }

    /**
     * Get all the studentResponses.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<StudentResponseDTO> findAll(Page page) {
        log.debug("Request to get all StudentResponses");
        return new Paged<>(StudentResponse.findAll().page(page))
            .map(studentResponse -> studentResponseMapper.toDto((StudentResponse) studentResponse));
    }


    /**
     * Get all the studentResponses with eager load of many-to-many relationships.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<StudentResponseDTO> findAllWithEagerRelationships(Page page) {
        var studentResponses = StudentResponse.findAllWithEagerRelationships().page(page).list();
        var totalCount = StudentResponse.findAll().count();
        var pageCount = StudentResponse.findAll().page(page).pageCount();
        return new Paged<>(page.index, page.size, totalCount, pageCount, studentResponses)
            .map(studentResponse -> studentResponseMapper.toDto((StudentResponse) studentResponse));
    }

        /**
     * Get all the studentResponses.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<StudentResponseDTO> findStudentResponsesbysheetIdAndquestionId(Page page, Long sheetId, List<Long> questionsId) {
        log.debug("Request to get all StudentResponses");
        return new Paged<>(StudentResponse.findStudentResponsesbysheetIdAndquestionId(sheetId, questionsId).page(page))
            .map(studentResponse -> studentResponseMapper.toDto((StudentResponse) studentResponse));
    }

    @Transactional
    public Optional<StudentResponseDTO> partialeNoteUpdate(StudentResponseNote notedto, Long id)  {
        log.debug("Request to patch partialeNoteUpdate " + id);
        var rOps = StudentResponse.findOne(id);
//        res.get()
        if (rOps.isPresent()){
            rOps.get().quarternote = Double.valueOf(notedto.getCurrentNote() * 4).intValue();

            return Optional.of( studentResponseMapper.toDto((StudentResponse) StudentResponse.persistOrUpdate(rOps.get())));
        } else {
            return Optional.empty();
        }


    }

}
