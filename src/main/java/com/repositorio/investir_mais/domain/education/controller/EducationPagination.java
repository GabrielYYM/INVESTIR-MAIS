package com.repositorio.investir_mais.domain.education.controller;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class EducationPagination {
    private static final Set<String> COURSE_SORTS = Set.of("id", "name", "createdAt", "updatedAt");
    private static final Set<String> LESSON_SORTS = Set.of("id", "title");

    private EducationPagination() {}

    static Pageable courses(Pageable pageable) {
        return validate(pageable, COURSE_SORTS);
    }

    static Pageable lessons(Pageable pageable) {
        return validate(pageable, LESSON_SORTS);
    }

    private static Pageable validate(Pageable pageable, Set<String> allowedSorts) {
        if (pageable.getOffset() > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A página solicitada excede o limite de paginação.");
        }
        for (var order : pageable.getSort()) {
            if (!allowedSorts.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenação inválido.");
            }
        }
        return pageable;
    }
}
