package com.repositorio.investir_mais.domain.token.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="one_time_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LoginToken {
    @Id @Column(name="token_value", length=64) private String tokenValue;
    @Column(nullable=false, length=320) private String username;
    @Column(name="expires_at", nullable=false) private Instant expiresAt;
}
