package fr.istic.service.mapper;

import fr.istic.domain.*;
import fr.istic.service.dto.PredictionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Prediction} and its DTO {@link PredictionDTO}.
 */
@Mapper(componentModel = "jakarta", uses = {QuestionMapper.class, ExamSheetMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PredictionMapper extends EntityMapper<PredictionDTO, Prediction> {

    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "sheet.id", target = "sheetId")
    PredictionDTO toDto(Prediction prediction);

    @Mapping(source = "questionId", target = "question")
    @Mapping(source = "sheetId", target = "sheet")
    Prediction toEntity(PredictionDTO predictionDTO);

    default Prediction fromId(Long id) {
        if (id == null) {
            return null;
        }
        Prediction prediction = new Prediction();
        prediction.id = id;
        return prediction;
    }
}
