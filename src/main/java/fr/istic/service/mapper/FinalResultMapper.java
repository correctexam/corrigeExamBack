package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.FinalResultDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link FinalResult} and its DTO {@link FinalResultDTO}.
 */
@Mapper(componentModel = "jakarta", uses = {StudentMapper.class, ExamMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface FinalResultMapper extends EntityMapper<FinalResultDTO, FinalResult> {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "exam.name", target = "examName")
    FinalResultDTO toDto(FinalResult finalResult);

    @Mapping(source = "studentId", target = "student")
    @Mapping(source = "examId", target = "exam")
    FinalResult toEntity(FinalResultDTO finalResultDTO);

    default FinalResult fromId(Long id) {
        if (id == null) {
            return null;
        }
        FinalResult finalResult = new FinalResult();
        finalResult.id = id;
        return finalResult;
    }
}
