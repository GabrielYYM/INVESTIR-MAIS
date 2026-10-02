package com.repositorio.investir_mais.domain.education.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.repositorio.investir_mais.domain.education.DTO.LessonRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonResponseDTO;
import com.repositorio.investir_mais.domain.education.model.Lesson;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    Lesson toEntity(LessonRequestDTO request);

    @Mapping(target = "courseId", source = "course.id")
    LessonResponseDTO toResponseDTO(Lesson lesson);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    void updateEntity(LessonRequestDTO request, @MappingTarget Lesson lesson);
}
