package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.CourseDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Course} and its DTO {@link CourseDTO}.
 */
@Mapper(componentModel = "cdi", uses = {UserMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CourseMapper extends EntityMapper<CourseDTO, Course> {

    @Mapping(source = "prof.id", target = "profId")
    @Mapping(source = "prof.login", target = "profLogin")
    CourseDTO toDto(Course course);

    @Mapping(target = "exams", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(source = "profId", target = "prof")
    Course toEntity(CourseDTO courseDTO);

    default Course fromId(Long id) {
        if (id == null) {
            return null;
        }
        Course course = new Course();
        course.id = id;
        return course;
    }
}
