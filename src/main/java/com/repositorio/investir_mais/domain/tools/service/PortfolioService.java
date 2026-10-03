package com.repositorio.investir_mais.domain.tools.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.tools.DTO.PortfolioResponseDTO;
import com.repositorio.investir_mais.domain.tools.mapper.PortfolioMapper;
import com.repositorio.investir_mais.domain.tools.model.Portfolio;
import com.repositorio.investir_mais.domain.tools.repository.PortfolioRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final PortfolioMapper portfolioMapper;

    public PortfolioResponseDTO findPortfolioById(UUID id) {
        return portfolioRepository.findById(id)
            .map(portfolioMapper::toDto)
            .orElseThrow(() -> new EntityNotFoundException("Portfolio não encontrado com o ID: " + id));
    }

    public PortfolioResponseDTO findByUserId(UUID userId) {
        return portfolioRepository.findByUserId_Id(userId)
            .map(portfolioMapper::toDto)
            .orElseThrow(() -> new EntityNotFoundException("Portfolio não encontrado para o usuário: " + userId));
    }

    public PortfolioResponseDTO createPortfolio(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(user);

        return portfolioMapper.toDto(portfolioRepository.save(portfolio));
    }

    public Portfolio findById(UUID id) {
        return portfolioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Portfolio não encontrado com o ID: " + id));
    }

    public PortfolioResponseDTO createPortfolioForUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con el ID: " + userId));

        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(user);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        return portfolioMapper.toDto(savedPortfolio);
    }
}
