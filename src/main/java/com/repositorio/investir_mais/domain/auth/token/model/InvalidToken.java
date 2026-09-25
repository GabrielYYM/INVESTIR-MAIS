package com.repositorio.investir_mais.domain.auth.token.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_INVALID_TOKEN")
@Getter
@Setter
public class InvalidToken {
    @Id
    @Column(length = 64)
    private String token;

    private Instant expiresAt;
}
