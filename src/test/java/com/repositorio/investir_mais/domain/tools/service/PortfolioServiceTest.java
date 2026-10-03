package com.repositorio.investir_mais.domain.tools.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.tools.DTO.PortfolioResponseDTO;
import com.repositorio.investir_mais.domain.tools.mapper.PortfolioMapper;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;
import com.repositorio.investir_mais.domain.tools.repository.PortfolioRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PortfolioMapper portfolioMapper;

    @InjectMocks
    private PortfolioService portfolioService;

    private UUID portfolioId;
    private UUID userId;
    private User user;
    private Portfolio portfolio;
    private PortfolioResponseDTO portfolioResponseDTO;

    @BeforeEach
    void setUp() {
        portfolioId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setUserId(user);
        portfolio.setAssets(new ArrayList<>());

        portfolioResponseDTO = new PortfolioResponseDTO(portfolioId, userId, new ArrayList<>());
    }

    @Test
    @DisplayName("Deve buscar portfolio por ID")
    void shouldFindPortfolioByIdSuccess() {
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(portfolioMapper.toDto(portfolio)).thenReturn(portfolioResponseDTO);

        PortfolioResponseDTO result = portfolioService.findPortfolioById(portfolioId);

        assertNotNull(result);
        assertEquals(portfolioId, result.id());
        verify(portfolioRepository, times(1)).findById(portfolioId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando portfolio não for encontrado por ID")
    void shouldThrowExceptionWhenPortfolioNotFoundById() {
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> portfolioService.findPortfolioById(portfolioId));
    }

    @Test
    @DisplayName("Deve buscar portfolio por User ID")
    void shouldFindByUserIdSuccess() {
        when(portfolioRepository.findByUserId_Id(userId)).thenReturn(Optional.of(portfolio));
        when(portfolioMapper.toDto(portfolio)).thenReturn(portfolioResponseDTO);

        PortfolioResponseDTO result = portfolioService.findByUserId(userId);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        verify(portfolioRepository, times(1)).findByUserId_Id(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando portfolio não for encontrado por User ID")
    void shouldThrowExceptionWhenPortfolioNotFoundByUserId() {
        when(portfolioRepository.findByUserId_Id(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> portfolioService.findByUserId(userId));
    }

    @Test
    @DisplayName("Deve criar portfolio para usuário com sucesso")
    void shouldCreatePortfolioSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        when(portfolioMapper.toDto(portfolio)).thenReturn(portfolioResponseDTO);

        PortfolioResponseDTO result = portfolioService.createPortfolio(userId);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException ao tentar criar portfolio para usuário inexistente")
    void shouldThrowExceptionWhenUserNotFoundOnCreatePortfolio() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> portfolioService.createPortfolio(userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(portfolioRepository, never()).save(any());
    }
}