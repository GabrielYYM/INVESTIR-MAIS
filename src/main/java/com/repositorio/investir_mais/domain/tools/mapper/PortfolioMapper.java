package com.repositorio.investir_mais.domain.tools.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.repositorio.investir_mais.domain.tools.DTO.PortfolioRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.PortfolioResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PortfolioMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "assets", ignore = true)
    Portfolio toEntity(PortfolioRequestDTO dto);

    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "assetIds", ignore = true)
    PortfolioResponseDTO toDto(Portfolio entity);
}
