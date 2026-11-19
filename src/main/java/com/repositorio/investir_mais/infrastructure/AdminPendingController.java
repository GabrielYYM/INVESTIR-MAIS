package com.repositorio.investir_mais.infrastructure;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPendingController {
    private final AdminPendingRepository pending;

    @GetMapping("/pending")
    @Operation(summary = "Consultar pendências administrativas", description = "Contadores de denúncias, candidaturas de contas ativas e prazos de recuperação. Não inclui dados pessoais.")
    public ResponseEntity<AdminPendingRepository.Summary> getAdminPendingSummary() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(pending.summarize(Instant.now()));
    }
}
