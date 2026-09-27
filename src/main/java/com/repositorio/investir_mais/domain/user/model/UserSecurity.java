package com.repositorio.investir_mais.domain.user.model;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;

import com.repositorio.investir_mais.domain.user.model.enums.UserRole;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSecurity {
    @NotBlank
    @Column(nullable = false, length = 255)
    @ToString.Exclude
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "two_factor_code", length = 6)
    @ToString.Exclude
    private String twoFactorCode;

    @Column(name = "two_factor_expiry")
    private LocalDateTime twoFactorExpiry;

    @Column(nullable = false, unique = true, length = 64)
    private String emailHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    public void generateTwoFactorCode(String code, LocalDateTime expiry) {
        this.twoFactorCode = code;
        this.twoFactorExpiry = expiry;
    }

    public void clearTwoFactorCode() {
        this.twoFactorCode = null;
        this.twoFactorExpiry = null;
    }
}
