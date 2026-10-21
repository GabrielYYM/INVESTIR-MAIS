package com.repositorio.investir_mais.infrastructure.token;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "app_one_time_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AccountToken {
    @Id
    @Column(name = "token_value", length = 64, nullable = false)
    private String tokenValue;
    @Column(length = 320, nullable = false)
    private String username;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(length = 32, nullable = false)
    private String purpose;
}
