package com.repositorio.investir_mais.domain.education.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.repositorio.investir_mais.domain.education.DTO.LessionRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessionResponseDTO;
import com.repositorio.investir_mais.domain.education.service.LessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lessions")
@RequiredArgsConstructor
public class LessionController {
    private final LessionService lessionService;

    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessionResponseDTO createLession(
            @PathVariable UUID courseId,
            @Valid @RequestBody LessionRequestDTO dto,
            Principal principal) {
        return lessionService.createLession(courseId, dto, principal.getName());
    }

    @GetMapping("/course/{courseId}")
    public Page<LessionResponseDTO> getLessionsByCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return lessionService.getLessionsByCourseId(courseId, pageable);
    }

    @GetMapping("/{lessionId}")
    public LessionResponseDTO getLessionById(@PathVariable UUID lessionId) {
        return lessionService.getLessionById(lessionId);
    }

    @PutMapping("/{lessionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessionResponseDTO updateLession(
            @PathVariable UUID lessionId,
            @Valid @RequestBody LessionRequestDTO dto,
            Principal principal) {
        return lessionService.updateLession(lessionId, dto, principal.getName());
    }

    @DeleteMapping("/{lessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteLession(
            @PathVariable UUID lessionId,
            Principal principal) {
        lessionService.deleteLession(lessionId, principal.getName());
    }
}
