package com.repositorio.investir_mais.domain.tools.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.repositorio.investir_mais.domain.tools.model.Asset;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByRole(AssetRole role);
}
