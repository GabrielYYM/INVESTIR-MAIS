package com.repositorio.investir_mais.domain.tools.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.repositorio.investir_mais.domain.tools.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    QuestionResponseDTO toResponseDTO(Question entity);

    @Mapping(target = "id", ignore = true)
    Question toEntity(QuestionRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(QuestionRequestDTO dto, @MappingTarget Question entity);
}
