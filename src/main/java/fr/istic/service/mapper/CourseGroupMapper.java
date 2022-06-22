package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.CourseGroupDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link CourseGroup} and its DTO {@link CourseGroupDTO}.
 */
@Mapper(componentModel = "cdi", uses = {StudentMapper.class, CourseMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CourseGroupMapper extends EntityMapper<CourseGroupDTO, CourseGroup> {

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.name", target = "courseName")
    CourseGroupDTO toDto(CourseGroup courseGroup);

    @Mapping(source = "courseId", target = "course")
    CourseGroup toEntity(CourseGroupDTO courseGroupDTO);

    default CourseGroup fromId(Long id) {
        if (id == null) {
            return null;
        }
        CourseGroup courseGroup = new CourseGroup();
        courseGroup.id = id;
        return courseGroup;
    }
}
