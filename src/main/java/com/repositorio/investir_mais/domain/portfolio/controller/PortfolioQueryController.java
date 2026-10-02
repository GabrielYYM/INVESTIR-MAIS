package com.repositorio.investir_mais.domain.portfolio.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.common.result.ServiceResult;
import com.repositorio.investir_mais.domain.portfolio.DTO.DashboardResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.DTO.RebalanceResponseDTO;
import com.repositorio.investir_mais.domain.portfolio.service.interfaces.PortfolioQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioQueryController {

    private final PortfolioQueryService portfolioQueryService;

    @GetMapping("/rebalance")
    public ResponseEntity<RebalanceResponseDTO> rebalance(
            @RequestParam(defaultValue = "0") BigDecimal aporteAmount) {
        ServiceResult<RebalanceResponseDTO> result = portfolioQueryService.calculateRebalance(aporteAmount);

        return switch (result) {
            case ServiceResult.Success<RebalanceResponseDTO> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<RebalanceResponseDTO> n -> throw new ErrorResponseException(
                    HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<RebalanceResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponseDTO> getSummary() {
        ServiceResult<DashboardResponseDTO> result = portfolioQueryService.getPortfolioSummary();

        return switch (result) {
            case ServiceResult.Success<DashboardResponseDTO> s -> ResponseEntity.ok(s.data());
            case ServiceResult.NotFound<DashboardResponseDTO> n -> throw new ErrorResponseException(
                    HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, n.message()), null);
            case ServiceResult.Error<DashboardResponseDTO> e -> throw new ErrorResponseException(HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message()), null);
        };
    }
}
