package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.ModuleResponse;
import com.repositorio.investir_mais.domain.education.service.ModuleQueryService;
import com.repositorio.investir_mais.infrastructure.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/education/modules")
public class ModuleQueryController {

    private final ModuleQueryService queryService;

    public ModuleQueryController(ModuleQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Tela de gestão/organização de módulos do professor, e dropdown
     * "Adicionar ao módulo" na tela de Detalhes do vídeo.
     */
    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'OWNER')")
    public List<ModuleResponse> listMine(@AuthenticationPrincipal UserDetailsImpl principal) {
        return queryService.listByProfessor(principal.getUser().getId());
    }

    /**
     * Listagem pública (aluno): só módulos com pelo menos um conteúdo
     * PUBLISHED.
     */
    @GetMapping
    public List<ModuleResponse> listVisible() {
        return queryService.listVisibleToStudents();
    }
}