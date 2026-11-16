package com.repositorio.investir_mais.domain.legal.controller;

import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import com.repositorio.investir_mais.domain.legal.DTO.LegalDocumentDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/legal")
@RequiredArgsConstructor
public class LegalDocumentsController {
    private final LegalDocumentsService legal;

    @PostMapping("/versions")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public LegalDocumentDTO.Release publish(@RequestBody LegalDocumentDTO.Release release) {
        return legal.publishNewRelease(release);
    }

    @GetMapping("/versions")
    public List<LegalDocumentDTO.Release> releases() { return legal.releases(); }

    @GetMapping("/current")
    public ResponseEntity<LegalDocumentDTO.Release> current() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(legal.current());
    }

    @GetMapping("/versions/{version}")
    public LegalDocumentDTO.Release release(@PathVariable int version) { return legal.release(version); }

    @GetMapping("/status")
    public ResponseEntity<LegalDocumentDTO.Status> status(Principal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(legal.status(principal.getName()));
    }

    @PostMapping("/acceptances")
    public LegalDocumentDTO.Status accept(Principal principal, @Valid @RequestBody LegalDocumentDTO.AcceptanceRequest request) {
        return legal.accept(principal.getName(), request.version());
    }

    @GetMapping("/acceptances")
    public ResponseEntity<List<LegalDocumentDTO.Acceptance>> history(Principal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(legal.history(principal.getName()));
    }
}
