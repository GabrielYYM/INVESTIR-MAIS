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

import com.repositorio.investir_mais.domain.education.DTO.LessonRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessonResponseDTO;
import com.repositorio.investir_mais.domain.education.service.LessonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lessions")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessionService;

    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonResponseDTO createLession(
            @PathVariable UUID courseId,
            @Valid @RequestBody LessonRequestDTO dto,
            Principal principal) {
        return lessionService.createLession(courseId, dto, principal.getName());
    }

    @GetMapping("/course/{courseId}")
    public Page<LessonResponseDTO> getLessionsByCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return lessionService.getLessionsByCourseId(courseId, pageable);
    }

    @GetMapping("/{lessionId}")
    public LessonResponseDTO getLessionById(@PathVariable UUID lessionId) {
        return lessionService.getLessionById(lessionId);
    }

    @PutMapping("/{lessionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonResponseDTO updateLession(
            @PathVariable UUID lessionId,
            @Valid @RequestBody LessonRequestDTO dto,
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
