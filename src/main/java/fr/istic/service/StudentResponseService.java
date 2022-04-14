package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.StudentResponse;
import fr.istic.service.dto.StudentResponseDTO;
import fr.istic.service.mapper.StudentResponseMapper;
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
public class StudentResponseService {

    private final Logger log = LoggerFactory.getLogger(StudentResponseService.class);

    @Inject
    StudentResponseMapper studentResponseMapper;

    @Transactional
    public StudentResponseDTO persistOrUpdate(StudentResponseDTO studentResponseDTO) {
        log.debug("Request to save StudentResponse : {}", studentResponseDTO);
        var studentResponse = studentResponseMapper.toEntity(studentResponseDTO);
        studentResponse = StudentResponse.persistOrUpdate(studentResponse);
        return studentResponseMapper.toDto(studentResponse);
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
        return StudentResponse.findByIdOptional(id)
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
     * Get all the studentResponses.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<StudentResponseDTO> findStudentResponsesbysheetIdAndquestionId(Page page, Long sheetId, Long questionId) {
        log.debug("Request to get all StudentResponses");
        return new Paged<>(StudentResponse.findStudentResponsesbysheetIdAndquestionId(sheetId, questionId).page(page))
            .map(studentResponse -> studentResponseMapper.toDto((StudentResponse) studentResponse));
    }




}
