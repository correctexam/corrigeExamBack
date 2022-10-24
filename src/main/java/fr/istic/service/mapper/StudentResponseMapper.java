package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.StudentResponseDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link StudentResponse} and its DTO {@link StudentResponseDTO}.
 */
@Mapper(componentModel = "cdi", uses = {QuestionMapper.class, ExamSheetMapper.class, TextCommentMapper.class, GradedCommentMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface StudentResponseMapper extends EntityMapper<StudentResponseDTO, StudentResponse> {

    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "question.numero", target = "questionNumero")
    @Mapping(source = "sheet.id", target = "sheetId")
    @Mapping(source = "sheet.name", target = "sheetName")
    @Mapping(source = "quarternote", target = "note", qualifiedByName = "quarternote2note")
    StudentResponseDTO toDto(StudentResponse studentResponse);

    @Mapping(target = "comments", ignore = true)
    @Mapping(source = "questionId", target = "question")
    @Mapping(source = "sheetId", target = "sheet")
    @Mapping(source = "note", target = "quarternote", qualifiedByName = "note2quarternote")
    StudentResponse toEntity(StudentResponseDTO studentResponseDTO);

    default StudentResponse fromId(Long id) {
        if (id == null) {
            return null;
        }
        StudentResponse studentResponse = new StudentResponse();
        studentResponse.id = id;
        return studentResponse;
    }

    @Named("quarternote2note")
    public static double quarterpoint2point(int quarternote) {
        return Integer.valueOf(quarternote).doubleValue() /4;
    }

    @Named("note2quarternote")
    public static int point2quarterpoint(double note) {
        return Double.valueOf(note *4).intValue();
    }
}
