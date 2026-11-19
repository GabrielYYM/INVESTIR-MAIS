package com.repositorio.investir_mais.domain.education.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

public final class CourseCategories {
    public static final List<String> VALUES = List.of("Investimentos", "Renda fixa", "Renda variável", "Fundos imobiliários",
        "Fundos de investimento", "Tesouro Direto", "Finanças pessoais", "Planejamento financeiro", "Economia", "Geral");
    private CourseCategories() {}
    public static String validate(String category, String previous) {
        String value = category == null || category.isBlank() ? (previous == null ? "Geral" : previous) : category.trim();
        // Existing courses retain their legacy category until their author selects a new one.
        if (!VALUES.contains(value) && !value.equals(previous)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma categoria disponível.");
        }
        return value;
    }
}
