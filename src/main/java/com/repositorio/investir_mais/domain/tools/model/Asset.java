package com.repositorio.investir_mais.domain.tools.model;

import java.math.BigDecimal;
import java.util.UUID;

import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_ASSETS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String ticker;

    private BigDecimal currentPositionValue;

    private BigDecimal quantity;

    private BigDecimal averagePrice;

    private int rawScore;

    private boolean isPositive;

    @Enumerated(EnumType.STRING)
    private AssetRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
}
