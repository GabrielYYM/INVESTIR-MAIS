package com.repositorio.investir_mais.domain.legal.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class LegalDocumentDTO {
    public record Section(@NotBlank String heading, @NotBlank String content) {}
    public record Document(
            @NotNull @Pattern(regexp = "[a-z][a-z0-9-]{0,63}") String id,
            @NotBlank String label,
            @NotBlank String title,
            @NotEmpty List<@NotNull @Valid Section> sections) {}
    public record Release(
            @Min(1) int version,
            @NotNull @PastOrPresent LocalDate publishedOn,
            @NotNull @Size(min = 2, max = 20) List<@NotNull @Valid Document> documents) {}
    public record Status(int currentVersion, boolean acceptanceRequired, Instant acceptedAt) {}
    public record Acceptance(int version, Instant acceptedAt) {}
    public record AcceptanceRequest(@Min(1) int version,
            @NotNull @AssertTrue(message = "Confirme a leitura dos documentos.") Boolean accepted) {}
}
