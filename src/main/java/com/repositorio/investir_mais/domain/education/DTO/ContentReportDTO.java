package com.repositorio.investir_mais.domain.education.DTO;

import com.repositorio.investir_mais.domain.education.model.ContentReport.*;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public class ContentReportDTO {
    @Schema(name = "ContentReportSubmit")
    public record Submit(@NotNull Target targetType, @NotNull UUID targetId, @NotNull UUID courseId,
                         @NotNull Reason reason, @NotBlank @Size(max = 2000) String description) {}
    @Schema(name = "ContentReportReceipt")
    public record Receipt(UUID id, Status status) {}
    @Schema(name = "ContentReportReview")
    public record Review(@NotNull Status status, @NotBlank @Size(max = 2000) String note, Boolean removeLesson) {}
}
