package com.repositorio.investir_mais.domain.question.model;

import java.util.UUID;

import com.repositorio.investir_mais.common.model.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade que representa uma pergunta qualitativa para avaliação de ativos.
 * Cada pergunta está vinculada a uma categoria de ativos e é utilizada para
 * calcular o Score (nota) dos ativos pertencentes àquela categoria.
 */
@Entity
@Table(name = "TB_QUESTION")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Question extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @ToString.Include
    private UUID id;

    @Column(nullable = false)
    private String text;

}
