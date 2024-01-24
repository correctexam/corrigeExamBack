package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Student;
import fr.istic.service.dto.StudentDTO;
import fr.istic.service.mapper.StudentMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class StudentService {

    private final Logger log = LoggerFactory.getLogger(StudentService.class);

    @Inject
    StudentMapper studentMapper;

    @Transactional
    public StudentDTO persistOrUpdate(StudentDTO studentDTO) {
        log.debug("Request to save Student : {}", studentDTO);
        var student = studentMapper.toEntity(studentDTO);
        student = Student.persistOrUpdate(student);
        return studentMapper.toDto(student);
    }

       @Transactional
    public Student persistOrUpdate(Student student) {
        student = Student.persistOrUpdate(student);
        return student;
    }


    /**
     * Delete the Student by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Student : {}", id);
        Student.findByIdOptional(id).ifPresent(student -> {
            student.delete();
        });
    }

    /**
     * Get one student by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<StudentDTO> findOne(Long id) {
        log.debug("Request to get Student : {}", id);
        return Student.findOneWithEagerRelationships(id)
            .map(student -> studentMapper.toDto((Student) student));
    }

    /**
     * Get all the students.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<StudentDTO> findAll(Page page) {
        log.debug("Request to get all Students");
        return new Paged<>(Student.findAll().page(page))
            .map(student -> studentMapper.toDto((Student) student));
    }

        /**
     * Get all the students.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<StudentDTO> findStudentsbyCourseId(Page page, long courseId) {
        log.debug("Request to get all Students by courseId");

        PanacheQuery<Student> s = Student.findStudentsbyCourseId(courseId).page(page);

        var p = new Paged<>(s);
        var res =  p
            .map(student -> studentMapper.toDto((Student) student));

            return res;
    }




    public Paged<StudentDTO> findStudentsbySheetId(Page page, long sheetId) {
        log.debug("Request to get all Students by sheetId");
        return new Paged<>(Student.findStudentsbySheetId(sheetId).page(page))
            .map(student -> studentMapper.toDto((Student) student));
    }


    /**
     * Get all the students with eager load of many-to-many relationships.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<StudentDTO> findAllWithEagerRelationships(Page page) {
        var students = Student.findAllWithEagerRelationships().page(page).list();
        var totalCount = Student.findAll().count();
        var pageCount = Student.findAll().page(page).pageCount();
        return new Paged<>(page.index, page.size, totalCount, pageCount, students)
            .map(student -> studentMapper.toDto((Student) student));
    }


}
