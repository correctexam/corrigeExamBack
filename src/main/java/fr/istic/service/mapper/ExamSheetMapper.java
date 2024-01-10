package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.ExamSheetDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link ExamSheet} and its DTO {@link ExamSheetDTO}.
 */
@Mapper(componentModel = "jakarta", uses = {ScanMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ExamSheetMapper extends EntityMapper<ExamSheetDTO, ExamSheet> {

    @Mapping(source = "scan.id", target = "scanId")
    @Mapping(source = "scan.name", target = "scanName")
    ExamSheetDTO toDto(ExamSheet examSheet);

    @Mapping(source = "scanId", target = "scan")
    @Mapping(target = "students", ignore = true)
    ExamSheet toEntity(ExamSheetDTO examSheetDTO);

    default ExamSheet fromId(Long id) {
        if (id == null) {
            return null;
        }
        ExamSheet examSheet = new ExamSheet();
        examSheet.id = id;
        return examSheet;
    }
}
