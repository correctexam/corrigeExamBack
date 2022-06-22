package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.StudentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Student} and its DTO {@link StudentDTO}.
 */
@Mapper(componentModel = "cdi", uses = {ExamSheetMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface StudentMapper extends EntityMapper<StudentDTO, Student> {


    @Mapping(target = "groups", ignore = true)
    Student toEntity(StudentDTO studentDTO);

    default Student fromId(Long id) {
        if (id == null) {
            return null;
        }
        Student student = new Student();
        student.id = id;
        return student;
    }
}
