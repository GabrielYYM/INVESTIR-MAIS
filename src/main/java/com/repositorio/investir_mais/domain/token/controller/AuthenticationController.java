package com.repositorio.investir_mais.domain.token.controller;

import com.repositorio.investir_mais.domain.token.service.AuthenticationService;
import com.repositorio.investir_mais.domain.token.DTO.TokenDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authentication;

    @GetMapping("/csrf")
    public TokenDTO.CsrfResponse csrf(HttpServletRequest request) {
        var token = (DeferredCsrfToken) request.getAttribute(DeferredCsrfToken.class.getName());
        return new TokenDTO.CsrfResponse(token.get().getToken());
    }

    @GetMapping("/auth/me")
    public UserDTO.Response currentUser(Authentication authentication) {
        return this.authentication.currentUser(authentication.getName());
    }
}
