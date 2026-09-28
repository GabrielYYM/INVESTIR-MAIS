package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.ContentResponse;
import com.repositorio.investir_mais.domain.education.service.EducationalContentQueryService;
import com.repositorio.investir_mais.infrastructure.security.UserDetailsImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de leitura. Sem @PreAuthorize restritivo de role no nível da
 * classe — qualquer usuário autenticado pode consultar; a diferença entre
 * "ver tudo" (professor) e "ver só publicado" (aluno) é resolvida pela
 * própria semântica de cada endpoint, não por role.
 */
@RestController
@RequestMapping("/api/education/contents")
public class EducationalContentQueryController {

    private final EducationalContentQueryService queryService;

    public EducationalContentQueryController(EducationalContentQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Tela "Conteúdo do Canal": tudo que o professor logado criou,
     * DRAFT ou PUBLISHED, com ou sem módulo.
     */
    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'OWNER')")
    public List<ContentResponse> listMine(@AuthenticationPrincipal UserDetailsImpl principal) {
        return queryService.listByProfessor(principal.getUser().getId());
    }

    @GetMapping("/{id}")
    public ContentResponse getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return queryService.getById(id, principal.getUser());
    }

    /**
     * Feed da Home do aluno: todo conteúdo publicado, mais recente primeiro.
     */
    @GetMapping("/published")
    public List<ContentResponse> listPublished() {
        return queryService.listPublished();
    }

    /**
     * Playlist de um módulo (painel lateral do player): só PUBLISHED,
     * na ordem definida pelo professor.
     */
    @GetMapping("/by-module/{moduleId}")
    public List<ContentResponse> listByModule(@PathVariable UUID moduleId) {
        return queryService.listPublishedByModule(moduleId);
    }
}