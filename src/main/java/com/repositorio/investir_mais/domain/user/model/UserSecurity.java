package com.repositorio.investir_mais.domain.user.model;

import com.repositorio.investir_mais.domain.user.model.enums.UserRole;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurity {
    @Email
    private String email;

    private String password;

    private UserRole role;

    private boolean emailVerified;
}
