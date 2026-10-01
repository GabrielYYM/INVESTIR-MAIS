package com.repositorio.investir_mais.domain.education.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.repositorio.investir_mais.domain.education.DTO.LessionRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessionResponseDTO;
import com.repositorio.investir_mais.domain.education.model.Lession;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    Lession toEntity(LessionRequestDTO request);

    @Mapping(target = "courseId", source = "course.id")
    LessionResponseDTO toResponseDTO(Lession lession);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    void updateEntity(LessionRequestDTO request, @MappingTarget Lession lession);
}
