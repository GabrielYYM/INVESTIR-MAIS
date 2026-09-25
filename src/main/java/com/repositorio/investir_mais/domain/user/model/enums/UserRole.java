package com.repositorio.investir_mais.domain.user.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Getter
@AllArgsConstructor
public enum UserRole {
    ADMIN("Admin", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))),
    USER("User", List.of(new SimpleGrantedAuthority("ROLE_USER")));

    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;
}
