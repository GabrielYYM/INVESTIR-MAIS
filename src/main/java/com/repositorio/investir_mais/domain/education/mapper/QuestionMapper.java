package com.repositorio.investir_mais.domain.education.mapper;

import com.repositorio.investir_mais.domain.education.DTO.QuestionDTO;
import com.repositorio.investir_mais.domain.education.model.*;
import org.mapstruct.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = List.class)
public interface QuestionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", source = "course")
    @Mapping(target = "position", source = "request.position")
    Question toEntity(Course course, QuestionDTO.Request request);

    void updateEntity(QuestionDTO.Request request, @MappingTarget Question question);

    @Mapping(target = "options", expression = "java(List.copyOf(question.getOptions()))")
    QuestionDTO.TeacherQuestion toDTO(Question question);

    @BeforeMapping
    default void validate(QuestionDTO.Request request) {
        if (request.correctOptionIndex() >= request.options().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Índice da alternativa correta inválido.");
        }
        if (request.options().stream().map(o -> o.strip().toLowerCase(Locale.ROOT)).distinct().count() != request.options().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As alternativas precisam ser diferentes.");
        }
    }
}
