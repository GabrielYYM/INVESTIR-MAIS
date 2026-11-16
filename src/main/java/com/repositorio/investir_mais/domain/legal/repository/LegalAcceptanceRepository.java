package com.repositorio.investir_mais.domain.legal.repository;

import com.repositorio.investir_mais.domain.legal.model.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalAcceptanceRepository extends JpaRepository<LegalAcceptance,LegalAcceptance.Id> {
    List<LegalAcceptance> findByIdUserIdOrderByIdVersionDesc(UUID userId);
}
