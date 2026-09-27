package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.ContentResponse;
import com.repositorio.investir_mais.domain.education.DTO.CreateContentRequest;
import com.repositorio.investir_mais.domain.education.DTO.UpdateContentRequest;
import com.repositorio.investir_mais.domain.education.service.EducationalContentCommandService;
import com.repositorio.investir_mais.domain.education.service.interfaces.MediaStorageService;
import com.repositorio.investir_mais.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * Endpoints de escrita do módulo de educação. Restritos a PROFESSOR/ADMIN/OWNER
 * — a checagem de posse do recurso (se é o dono mesmo) fica no Service,
 * aqui só validamos a role.
 *
 * O principal autenticado é sempre um UserDetailsImpl (ver SecurityFilter),
 * nunca um User direto — por isso @AuthenticationPrincipal UserDetailsImpl
 * + principal.getUser(), no mesmo padrão usado em UserCommandController
 * ("authentication.principal.user.id" no SpEL de outros @PreAuthorize).
 */
@RestController
@RequestMapping("/api/education/contents")
@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'OWNER')")
public class EducationalContentCommandController {

    private final EducationalContentCommandService commandService;
    private final MediaStorageService mediaStorageService;

    public EducationalContentCommandController(
            EducationalContentCommandService commandService,
            MediaStorageService mediaStorageService
    ) {
        this.commandService = commandService;
        this.mediaStorageService = mediaStorageService;
    }

    /**
     * Chamado pelo front ANTES do upload: gera a assinatura para o
     * signed upload direto no Cloudinary.
     */
    @PostMapping("/upload-signature")
    public ResponseEntity<MediaStorageService.UploadSignature> getUploadSignature() {
        return ResponseEntity.ok(mediaStorageService.generateUploadSignature());
    }

    /**
     * Chamado pelo front assim que o Cloudinary confirma o upload.
     * Cria o registro já como DRAFT.
     */
    @PostMapping
    public ResponseEntity<ContentResponse> create(
            @Valid @RequestBody CreateContentRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        ContentResponse response = commandService.create(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Tela "Detalhes do vídeo": título, descrição, miniatura e módulo.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContentRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(commandService.update(id, request, principal.getUser()));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ContentResponse> publish(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(commandService.publish(id, principal.getUser()));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ContentResponse> unpublish(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(commandService.unpublish(id, principal.getUser()));
    }

    /**
     * Deleção física (registro + arquivo no Cloudinary), acionada pelo
     * menu de opções (3 pontinhos) na listagem.
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