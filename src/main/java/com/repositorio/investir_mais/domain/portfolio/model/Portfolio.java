package com.repositorio.investir_mais.domain.portfolio.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.repositorio.investir_mais.common.model.Auditable;
import com.repositorio.investir_mais.domain.asset.model.AssetCategory;
import com.repositorio.investir_mais.domain.user.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade que representa a Carteira de Investimentos de um usuário.
 * Atua como o agregador raiz de categorias e ativos, centralizando a lógica
 * de validação de alocação total (limite de 100%).
 */
@Entity
@Table(name = "TB_PORTFOLIO")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Portfolio extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @ToString.Include
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetCategory> categories;

    public void validateAndAddCategoryTarget(BigDecimal newValue, UUID excludeCategoryId) {
        if (categories == null) {
            if (newValue.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("O percentual alvo total das categorias não pode exceder 100%.");
            }
            return;
        }

        BigDecimal currentTotal = categories.stream()
                .filter(c -> excludeCategoryId == null || !c.getId().equals(excludeCategoryId))
                .map(AssetCategory::getTargetPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentTotal.add(newValue).compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("O percentual alvo total das categorias não pode exceder 100%.");
        }
    }
}
