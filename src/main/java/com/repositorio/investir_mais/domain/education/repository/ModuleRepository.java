package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.Module;
import com.repositorio.investir_mais.domain.education.model.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<Module, UUID> {

    /**
     * Módulos de um professor específico, na ordem definida por ele.
     * Usado na tela de gestão/organização de módulos e no dropdown
     * "Adicionar ao módulo" da tela de Detalhes do vídeo.
     */
    List<Module> findAllByProfessorIdOrderByOrderIndexAsc(UUID professorId);

    /**
     * Todos os módulos que têm ao menos um conteúdo publicado, na ordem
     * de exibição. Usado na listagem de módulos visível ao aluno.
     *
     * Não há relação bidirecional mapeada em Module (evitamos isso de
     * propósito para não acoplar as duas entidades), então a busca é
     * feita via join explícito em JPQL em vez de uma derived query.
     */
    @Query("""
        SELECT DISTINCT m FROM Module m
        JOIN EducationalContent c ON c.module = m
        WHERE c.status = :status
        ORDER BY m.orderIndex ASC
        """)
    List<Module> findModulesWithContentStatus(@Param("status") ContentStatus status);
}
