package com.repositorio.investir_mais.domain.education.mapper;

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LessonMapper {

    Lesson toEntity(LessonDTO.Request request);

    @Mapping(target = "courseId", source = "course.id")
    LessonDTO.Response toDTO(Lesson lesson);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LessonDTO.Request request, @MappingTarget Lesson lesson);
}