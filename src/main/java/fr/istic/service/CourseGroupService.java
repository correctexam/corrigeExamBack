package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.CourseGroup;
import fr.istic.service.dto.CourseGroupDTO;
import fr.istic.service.mapper.CourseGroupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class CourseGroupService {

    private final Logger log = LoggerFactory.getLogger(CourseGroupService.class);

    @Inject
    CourseGroupMapper courseGroupMapper;

    @Transactional
    public CourseGroupDTO persistOrUpdate(CourseGroupDTO courseGroupDTO) {
        log.debug("Request to save CourseGroup : {}", courseGroupDTO);
        var courseGroup = courseGroupMapper.toEntity(courseGroupDTO);
        courseGroup = CourseGroup.persistOrUpdate(courseGroup);
        return courseGroupMapper.toDto(courseGroup);
    }

    /**
     * Delete the CourseGroup by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete CourseGroup : {}", id);
        CourseGroup.findByIdOptional(id).ifPresent(courseGroup -> {
            courseGroup.delete();
        });
    }

    /**
     * Get one courseGroup by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<CourseGroupDTO> findOne(Long id) {
        log.debug("Request to get CourseGroup : {}", id);
        return CourseGroup.findOneWithEagerRelationships(id)
            .map(courseGroup -> courseGroupMapper.toDto((CourseGroup) courseGroup));
    }

    /**
     * Get all the courseGroups.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CourseGroupDTO> findAll(Page page) {
        log.debug("Request to get all CourseGroups");
        return new Paged<>(CourseGroup.findAll().page(page))
            .map(courseGroup -> courseGroupMapper.toDto((CourseGroup) courseGroup));
    }


    /**
     * Get all the courseGroups with eager load of many-to-many relationships.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<CourseGroupDTO> findAllWithEagerRelationships(Page page) {
        var courseGroups = CourseGroup.findAllWithEagerRelationships().page(page).list();
        var totalCount = CourseGroup.findAll().count();
        var pageCount = CourseGroup.findAll().page(page).pageCount();
        return new Paged<>(page.index, page.size, totalCount, pageCount, courseGroups)
            .map(courseGroup -> courseGroupMapper.toDto((CourseGroup) courseGroup));
    }


}
