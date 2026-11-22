package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.service.ContentTrashService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/api/trash") @RequiredArgsConstructor @PreAuthorize("hasRole('ADMIN')")
public class ContentTrashController {
    private final ContentTrashService service;
    @GetMapping
    public Map<String, Object> listTrashContent(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return service.list(page, size); }
    @PostMapping("/{entity}/{id}/restore") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreTrashContent(@PathVariable String entity, @PathVariable UUID id, Principal principal) {
        service.restore(entity, id, principal.getName());
    }
}
