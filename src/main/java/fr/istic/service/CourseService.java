package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Course;
import fr.istic.domain.User;
import fr.istic.service.dto.CourseDTO;
import fr.istic.service.mapper.CourseMapper;
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
public class CourseService {

    private final Logger log = LoggerFactory.getLogger(CourseService.class);

    @Inject
    CourseMapper courseMapper;

    @Transactional
    public CourseDTO persistOrUpdate(CourseDTO courseDTO) {
        log.debug("Request to save Course : {}", courseDTO);
        var course = courseMapper.toEntity(courseDTO);
        course = Course.persistOrUpdate(course);
        return courseMapper.toDto(course);
    }

    /**
     * Delete the Course by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Course : {}", id);
        Course.findByIdOptional(id).ifPresent(course -> {
            course.delete();
        });
    }

    /**
     * Get one course by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<CourseDTO> findOne(Long id) {
        log.debug("Request to get Course : {}", id);
        return Course.findByIdOptional(id)
            .map(course -> courseMapper.toDto((Course) course));
    }

    /**
     * Get all the courses.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CourseDTO> findAll(Page page) {
        log.debug("Request to get all Courses");
        return new Paged<>(Course.findAll().page(page))
            .map(course -> courseMapper.toDto((Course) course));
    }

        /**
     * Get all the courses.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CourseDTO> findAll4User(Page page, User u) {
        log.debug("Request to get all Courses");
        return new Paged<>(Course.findByProfIsCurrentUser(u.login).page(page))
            .map(course -> courseMapper.toDto((Course) course));
    }




}
