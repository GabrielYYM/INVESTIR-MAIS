package com.repositorio.investir_mais.domain.token.model;

import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/** Read-only account mapping: token locks must also cover logically deleted accounts. */
@Entity @Table(name="tb_user") @Immutable
public class TokenAccount {
    @Id private UUID id;
    private String email;
    protected TokenAccount() {}
}
