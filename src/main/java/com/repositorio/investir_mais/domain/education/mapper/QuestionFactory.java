package com.repositorio.investir_mais.domain.education.mapper;

import com.repositorio.investir_mais.domain.education.DTO.QuestionDTO;
import com.repositorio.investir_mais.domain.education.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
import java.util.ArrayList;

public final class QuestionFactory {
    private QuestionFactory() {}

    public static Question create(Course course, QuestionDTO.Request request) {
        Question question = new Question();
        question.setCourse(course);
        apply(question, request);
        return question;
    }

    public static void apply(Question question, QuestionDTO.Request request) {
        if (request.correctOptionIndex() >= request.options().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Índice da alternativa correta inválido.");
        }
        if (request.options().stream().map(o -> o.strip().toLowerCase(Locale.ROOT)).distinct().count() != request.options().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As alternativas precisam ser diferentes.");
        }
        question.setPrompt(request.prompt());
        question.setAfterLesson(request.afterLesson()); question.setPosition(request.position());
        question.setOptions(new ArrayList<>(request.options()));
        question.setCorrectOptionIndex(request.correctOptionIndex());
    }
}
