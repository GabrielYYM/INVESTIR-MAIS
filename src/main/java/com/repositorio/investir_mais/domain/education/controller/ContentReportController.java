package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.ContentReportDTO.*;
import com.repositorio.investir_mais.domain.education.model.ContentReport;
import com.repositorio.investir_mais.domain.education.model.ContentReport.Status;
import com.repositorio.investir_mais.domain.education.repository.ContentReportRepository;
import com.repositorio.investir_mais.domain.education.service.ContentReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.UUID;

@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ContentReportController {
    private final ContentReportService service;
    private final ContentReportRepository reports;

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("isAuthenticated()")
    public Receipt submitContentReport(Principal principal, @Valid @RequestBody Submit request) { return service.submit(principal.getName(), request); }

    @GetMapping @PreAuthorize("hasRole('ADMIN')") @Transactional(readOnly = true)
    public Page<ContentReport> listContentReports(@RequestParam(defaultValue = "OPEN") Status status,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return reports.findByStatus(status, PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id"))));
    }

    @PostMapping("/{id}/review") @PreAuthorize("hasRole('ADMIN')")
    public ContentReport reviewContentReport(@PathVariable UUID id, Principal principal, @Valid @RequestBody Review request) {
        return service.review(id, principal.getName(), request);
    }
}
