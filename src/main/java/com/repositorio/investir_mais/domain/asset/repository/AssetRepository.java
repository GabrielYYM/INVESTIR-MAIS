package com.repositorio.investir_mais.domain.asset.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.repositorio.investir_mais.domain.asset.model.Asset;
import com.repositorio.investir_mais.domain.asset.repository.projection.AssetScoreProjection;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID>{
    List<Asset> findAllByCategoryId(UUID categoryId);
    
    @Query("""
        SELECT a.id AS id, a.ticker AS ticker, 
               COALESCE(SUM(CASE WHEN e.isPositive = true THEN 1 ELSE -1 END), 0) AS rawScore
        FROM Asset a
        LEFT JOIN a.evaluations e
        WHERE a.category.id = :categoryId
        GROUP BY a.id, a.ticker
    """)
    List<AssetScoreProjection> findConsolidatedScoresByCategoryId(@Param("categoryId") UUID categoryId);
}
