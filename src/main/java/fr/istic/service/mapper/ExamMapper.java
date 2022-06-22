package fr.istic.service.mapper;


import fr.istic.domain.*;
import fr.istic.service.dto.ExamDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Exam} and its DTO {@link ExamDTO}.
 */
@Mapper(componentModel = "cdi", uses = {TemplateMapper.class, ZoneMapper.class, ScanMapper.class, CourseMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ExamMapper extends EntityMapper<ExamDTO, Exam> {

    @Mapping(source = "template.id", target = "templateId")
    @Mapping(source = "template.name", target = "templateName")
    @Mapping(source = "idzone.id", target = "idzoneId")
    @Mapping(source = "namezone.id", target = "namezoneId")
    @Mapping(source = "firstnamezone.id", target = "firstnamezoneId")
    @Mapping(source = "notezone.id", target = "notezoneId")
    @Mapping(source = "scanfile.id", target = "scanfileId")
    @Mapping(source = "scanfile.name", target = "scanfileName")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.name", target = "courseName")
    ExamDTO toDto(Exam exam);

    @Mapping(source = "templateId", target = "template")
    @Mapping(source = "idzoneId", target = "idzone")
    @Mapping(source = "namezoneId", target = "namezone")
    @Mapping(source = "firstnamezoneId", target = "firstnamezone")
    @Mapping(source = "notezoneId", target = "notezone")
    @Mapping(source = "scanfileId", target = "scanfile")
    @Mapping(target = "questions", ignore = true)
    @Mapping(source = "courseId", target = "course")
    Exam toEntity(ExamDTO examDTO);

    default Exam fromId(Long id) {
        if (id == null) {
            return null;
        }
        Exam exam = new Exam();
        exam.id = id;
        return exam;
    }
}
