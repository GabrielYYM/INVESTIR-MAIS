package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.ModuleRequest;
import com.repositorio.investir_mais.domain.education.DTO.ModuleResponse;
import com.repositorio.investir_mais.domain.education.service.ModuleCommandService;
import com.repositorio.investir_mais.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/education/modules")
@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'OWNER')")
public class ModuleCommandController {

    private final ModuleCommandService commandService;

    public ModuleCommandController(ModuleCommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * Usado tanto pela tela de gestão de módulos quanto pela opção
     * "criar novo módulo" no dropdown da tela de Detalhes do vídeo.
     */
    @PostMapping
    public ResponseEntity<ModuleResponse> create(
            @Valid @RequestBody ModuleRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        ModuleResponse response = commandService.create(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ModuleRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(commandService.update(id, request, principal.getUser()));
    }

    /**
     * Exclui o módulo; os vídeos associados não são apagados, só perdem
     * o vínculo (voltam a aparecer apenas na listagem do professor).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        commandService.delete(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
