package com.repositorio.investir_mais.domain.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.domain.audit.DTO.AuditLogResponseDTO;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.service.interfaces.AuditQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoria do Sistema", description = "Endpoints administrativos para visualização da trilha de auditoria e segurança")
public class AuditQueryController {

    private final AuditQueryService auditQueryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista o histórico de auditoria do sistema",
            description = "Retorna logs de auditoria paginados, permitindo filtrar por ação ou e-mail do autor.")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAuditLogs(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        if (action != null) {
            return ResponseEntity.ok(auditQueryService.findByAction(action, pageable));
        }

        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(auditQueryService.findByActorEmail(email, pageable));
        }

        return ResponseEntity.ok(auditQueryService.findAll(pageable));
    }
}
