package com.repositorio.investir_mais.domain.education.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public class QuestionDTO {
    @Schema(name = "QuestionRequest")
    public record Request(@NotBlank @Size(max = 5000) String prompt,
                          @Min(0) @Max(10000) int afterLesson,
                          @Min(0) @Max(10000) int position,
                          @NotNull @Size(min = 2, max = 6) List<@NotBlank @Size(max = 2000) String> options,
                          @NotNull @Min(0) @Max(5) Integer correctOptionIndex) {}
    public record AnswerRequest(@NotNull @Min(0) @Max(5) Integer optionIndex) {}
    public record StudentQuestion(UUID id, String prompt, int afterLesson, int position,
                                  boolean locked, boolean passed, List<String> options) {}
    public record TeacherQuestion(UUID id, String prompt, int afterLesson, int position,
                                  List<String> options, int correctOptionIndex) {}
    public record AnswerResponse(UUID questionId, boolean passed) {}
}
