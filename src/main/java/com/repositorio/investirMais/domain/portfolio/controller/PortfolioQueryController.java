package com.repositorio.investirMais.domain.portfolio.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investirMais.common.result.ServiceResult;
import com.repositorio.investirMais.domain.portfolio.DTO.DashboardResponseDTO;
import com.repositorio.investirMais.domain.portfolio.DTO.RebalanceResponseDTO;
import com.repositorio.investirMais.domain.portfolio.service.interfaces.PortfolioQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(name = "Consultas de Carteira", description = "Endpoints para visualização da carteira e cálculo de rebalanceamento")
public class PortfolioQueryController {

    private final PortfolioQueryService portfolioQueryService;

    @GetMapping("/rebalance")
    @Operation(summary = "Calcula o rebalanceamento da carteira", description = "Baseado no valor do aporte, sugere onde investir para atingir os alvos definidos.")
    @ApiResponse(responseCode = "200", description = "Rebalanceamento calculado com sucesso")
    public ResponseEntity<RebalanceResponseDTO> rebalance(@RequestParam(defaultValue = "0") BigDecimal aporteAmount) {
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
    @Operation(summary = "Fornece o resumo estratégico da carteira", description = "Retorna o retrato estruturado: Valor Total, Alvos Proporcionais e Distribuição por Categoria.")
    @ApiResponse(responseCode = "200", description = "Resumo da carteira retornado com sucesso")
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
