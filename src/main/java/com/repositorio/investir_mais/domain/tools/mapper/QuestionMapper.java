package com.repositorio.investir_mais.domain.tools.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.repositorio.investir_mais.domain.tools.DTO.QuestionDTO;
import com.repositorio.investir_mais.domain.tools.model.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    QuestionDTO.Response toResponseDTO(Question entity);

    @Mapping(target = "id", ignore = true)
    Question toEntity(QuestionDTO.Request dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(QuestionDTO.Request dto, @MappingTarget Question entity);
}