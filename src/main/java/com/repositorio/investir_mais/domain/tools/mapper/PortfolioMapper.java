package com.repositorio.investir_mais.domain.tools.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.repositorio.investir_mais.domain.tools.DTO.PortfolioDTO;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PortfolioMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "assets", ignore = true)
    Portfolio toEntity(PortfolioDTO.Request dto);

    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "assetIds", ignore = true)
    PortfolioDTO.Response toDto(Portfolio entity);
}