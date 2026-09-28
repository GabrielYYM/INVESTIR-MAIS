package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.EducationalContent;
import com.repositorio.investir_mais.domain.education.model.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EducationalContentRepository extends JpaRepository<EducationalContent, UUID> {

    /**
     * Tudo que o professor já criou, com ou sem módulo, DRAFT ou PUBLISHED.
     * Usado na tela de listagem de conteúdo do canal.
     */
    List<EducationalContent> findAllByProfessorId(UUID professorId);

    /**
     * Conteúdos publicados de um módulo específico, na ordem da playlist.
     * Usado na tela do player (painel lateral) para o aluno.
     */
    List<EducationalContent> findAllByModuleIdAndStatusOrderByOrderInModuleAsc(
            UUID moduleId, ContentStatus status
    );

    /**
     * Conteúdos do professor filtrados por status, ex: só DRAFTs pendentes de publicação.
     */
    List<EducationalContent> findAllByProfessorIdAndStatus(UUID professorId, ContentStatus status);

    /**
     * Feed geral de conteúdo publicado (mais recente primeiro), independente
     * de módulo ou professor. Usado na Home do aluno (seções "Descubra" e
     * "Mais vistos").
     */
    List<EducationalContent> findAllByStatusOrderByCreatedAtDesc(ContentStatus status);
}