package com.repositorio.investir_mais.domain.question.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.repositorio.investir_mais.domain.question.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.question.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.question.model.Question;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper {

    @Mapping(target = "assetCategory", ignore = true)
    Question toEntity(QuestionRequestDTO request);

    QuestionResponseDTO toResponse(Question question);
}
