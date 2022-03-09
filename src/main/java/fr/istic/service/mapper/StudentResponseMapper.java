package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.StudentResponseDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link StudentResponse} and its DTO {@link StudentResponseDTO}.
 */
@Mapper(componentModel = "cdi", uses = {QuestionMapper.class, StudentMapper.class})
public interface StudentResponseMapper extends EntityMapper<StudentResponseDTO, StudentResponse> {

    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "question.numero", target = "questionNumero")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    StudentResponseDTO toDto(StudentResponse studentResponse);

    @Mapping(target = "comments", ignore = true)
    @Mapping(source = "questionId", target = "question")
    @Mapping(source = "studentId", target = "student")
    StudentResponse toEntity(StudentResponseDTO studentResponseDTO);

    default StudentResponse fromId(Long id) {
        if (id == null) {
            return null;
        }
        StudentResponse studentResponse = new StudentResponse();
        studentResponse.id = id;
        return studentResponse;
    }
}
