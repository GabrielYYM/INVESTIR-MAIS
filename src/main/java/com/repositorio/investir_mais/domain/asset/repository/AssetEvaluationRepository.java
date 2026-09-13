package com.repositorio.investir_mais.domain.asset.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.repositorio.investir_mais.domain.asset.model.AssetEvaluation;

@Repository
public interface AssetEvaluationRepository extends JpaRepository<AssetEvaluation, UUID> {
    List<AssetEvaluation> findAllByAssetId(UUID assetId);
}
