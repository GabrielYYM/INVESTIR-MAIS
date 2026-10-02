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
    // Seed único, definido direto no banco pelos desenvolvedores. Único que
    // pode promover/rebaixar ADMIN.
    OWNER("Owner", List.of(
            new SimpleGrantedAuthority("ROLE_OWNER"),
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_ALUNO"))),
    // Promove/rebaixa PROFESSOR (aluno <-> professor). Não promove/rebaixa OWNER/ADMIN.
    ADMIN("Admin", List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_ALUNO"))),
    // Cria/edita/publica/exclui conteúdo educacional e módulos próprios.
    PROFESSOR("Professor", List.of(
            new SimpleGrantedAuthority("ROLE_PROFESSOR"),
            new SimpleGrantedAuthority("ROLE_ALUNO"))),
    // Papel padrão de todo usuário cadastrado publicamente.
    ALUNO("Aluno", List.of(new SimpleGrantedAuthority("ROLE_ALUNO")));

    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;
}
