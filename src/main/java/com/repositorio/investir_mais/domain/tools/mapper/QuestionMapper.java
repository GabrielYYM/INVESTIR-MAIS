package com.repositorio.investir_mais.domain.tools.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.repositorio.investir_mais.domain.tools.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(target = "lessionId", source = "category.id")
    QuestionResponseDTO toResponseDTO(Question entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Question toEntity(QuestionRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(QuestionRequestDTO dto, @MappingTarget Question entity);
}