package com.repositorio.investir_mais.domain.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.repositorio.investir_mais.domain.user.service.TeacherApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.*;

@RestController @RequestMapping("/api/teacher-applications") @RequiredArgsConstructor
public class TeacherApplicationController {
    private final TeacherApplicationService service;
    public record Review(@NotNull Boolean approve, @NotBlank @Size(max=2000) String reason) {}
    @GetMapping("/requirements") public List<String> teacherDocumentRequirements() { return service.types(); }
    @GetMapping("/mine") public ResponseEntity<TeacherApplicationService.ApplicationView> myTeacherApplication(Principal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.mine(principal.getName()));
    }
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public TeacherApplicationService.ApplicationView submitTeacherApplication(Principal principal, @RequestParam List<String> types, @RequestPart List<MultipartFile> files) { return service.submit(principal.getName(),types,files); }
    @GetMapping public Page<TeacherApplicationService.ApplicationView> getTeacherApplications(@org.springdoc.core.annotations.ParameterObject Pageable pageable) { return service.list(pageable); }
    @PostMapping("/{id}/review") public TeacherApplicationService.ApplicationView reviewTeacherApplication(@PathVariable UUID id, @Valid @RequestBody Review request, Principal principal) { return service.review(id,request.approve(),request.reason(),principal.getName()); }
    @GetMapping("/{id}/documents/{documentId}") public ResponseEntity<byte[]> downloadTeacherDocument(@PathVariable UUID id,@PathVariable UUID documentId,Principal principal) { return service.document(id,documentId,principal.getName()); }
}
