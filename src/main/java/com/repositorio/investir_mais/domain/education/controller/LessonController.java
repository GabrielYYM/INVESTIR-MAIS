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

import com.repositorio.investir_mais.domain.education.DTO.LessonDTO;
import com.repositorio.investir_mais.domain.education.service.LessonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;

    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response createLesson(
            @PathVariable UUID courseId,
            @Valid @RequestBody LessonDTO.Request dto,
            Principal principal) {
        return lessonService.createLesson(courseId, dto, principal.getName());
    }

    @GetMapping("/course/{courseId}")
    public Page<LessonDTO.Response> getLessonsByCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return lessonService.getLessonsByCourseId(courseId, pageable);
    }

    @GetMapping("/{lessonId}")
    public LessonDTO.Response getLessonById(@PathVariable UUID lessonId) {
        return lessonService.getLessonById(lessonId);
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public LessonDTO.Response updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonDTO.Request dto,
            Principal principal) {
        return lessonService.updateLesson(lessonId, dto, principal.getName());
    }

    @DeleteMapping("/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteLesson(
            @PathVariable UUID lessonId,
            Principal principal) {
        lessonService.deleteLesson(lessonId, principal.getName());
    }
}