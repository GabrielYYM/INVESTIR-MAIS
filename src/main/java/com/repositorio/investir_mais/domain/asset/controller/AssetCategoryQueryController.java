package com.repositorio.investir_mais.domain.asset.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.asset.DTO.CategoryResponseDTO;
import com.repositorio.investir_mais.domain.asset.service.interfaces.AssetCategoryQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assets/categories")
@RequiredArgsConstructor
public class AssetCategoryQueryController {
    private final AssetCategoryQueryService queryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listCategories() {
        ServiceResult<List<CategoryResponseDTO>> result = queryService.listUserCategories();

        return switch (result) {
            case ServiceResult.Success<List<CategoryResponseDTO>> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<List<CategoryResponseDTO>> n -> throw new ErrorResponseException(
                    HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<List<CategoryResponseDTO>> e ->
                throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
