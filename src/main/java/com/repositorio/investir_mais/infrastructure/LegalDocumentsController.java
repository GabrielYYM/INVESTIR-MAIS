package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.LegalDocumentsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    public record AcceptanceRequest(@Min(1) int version,
            @NotNull @AssertTrue(message = "Confirme a leitura dos documentos.") Boolean accepted) {}

    @GetMapping("/current")
    public ResponseEntity<LegalDocumentsService.Release> current() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(legal.current());
    }

    @GetMapping("/versions/{version}")
    public LegalDocumentsService.Release release(@PathVariable int version) { return legal.release(version); }

    @GetMapping("/status")
    public ResponseEntity<LegalDocumentsService.Status> status(Principal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(legal.status(principal.getName()));
    }

    @PostMapping("/acceptances")
    public LegalDocumentsService.Status accept(Principal principal, @Valid @RequestBody AcceptanceRequest request) {
        return legal.accept(principal.getName(), request.version());
    }

    @GetMapping("/acceptances")
    public ResponseEntity<List<LegalDocumentsService.Acceptance>> history(Principal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(legal.history(principal.getName()));
    }
}
