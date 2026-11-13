package com.repositorio.investir_mais.domain.education.mapper;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.model.Course;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

    @Mapping(target = "questions", ignore = true)
    Course toEntity(CourseDTO.Request request);

    @Mapping(target = "professorId", source = "professor.id")
    CourseDTO.Response toDTO(Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "questions", ignore = true)
    void updateEntity(CourseDTO.Request request, @MappingTarget Course course);
}
