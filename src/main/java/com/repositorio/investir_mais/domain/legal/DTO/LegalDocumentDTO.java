package com.repositorio.investir_mais.domain.legal.DTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class LegalDocumentDTO {
    public record Section(String heading, String content) {}
    public record Document(String id, String label, String title, List<Section> sections) {}
    public record Release(int version, LocalDate publishedOn, List<Document> documents) {}
    public record Status(int currentVersion, boolean acceptanceRequired, Instant acceptedAt) {}
    public record Acceptance(int version, Instant acceptedAt) {}
    public record AcceptanceRequest(@Min(1) int version,
            @NotNull @AssertTrue(message = "Confirme a leitura dos documentos.") Boolean accepted) {}
}
