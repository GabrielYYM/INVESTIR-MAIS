package com.repositorio.investir_mais.domain.tools.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.repositorio.investir_mais.domain.tools.DTO.AssetRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.AssetResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.Asset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssetMapper {
    AssetResponseDTO toDTO(Asset entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rawScore", ignore = true)
    @Mapping(target = "portfolio", ignore = true)
    Asset toEntity(AssetRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rawScore", ignore = true)
    @Mapping(target = "portfolio", ignore = true)
    void updateEntityFromDto(AssetRequestDTO dto, @MappingTarget Asset entity);
}
