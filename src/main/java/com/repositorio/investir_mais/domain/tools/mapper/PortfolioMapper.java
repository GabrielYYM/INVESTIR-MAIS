package com.repositorio.investir_mais.domain.tools.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.repositorio.investir_mais.domain.tools.DTO.PortfolioRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.PortfolioResponseDTO;
import com.repositorio.investir_mais.domain.tools.model.Category;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PortfolioMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "listCategory", ignore = true)
    Portfolio toEntity(PortfolioRequestDTO dto);

    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "categoryId", source = "listCategory", qualifiedByName = "mapCategoryListToIdList")
    PortfolioResponseDTO toDto(Portfolio entity);

    @Named("mapCategoryListToIdList")
    default List<UUID> mapCategoryListToIdList(List<Category> listCategory) {
        if (listCategory == null) {
            return null;
        }
        return listCategory.stream()
                .map(Category::getId)
                .toList();
    }
}