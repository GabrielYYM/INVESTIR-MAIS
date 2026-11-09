package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final WebApplicationProperties properties;

    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest request) {
        var token = (DeferredCsrfToken) request.getAttribute(DeferredCsrfToken.class.getName());
        return Map.of("token", token.get().getToken());
    }

    @GetMapping("/auth/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        var user = userMapper.toDTO(userService.findByEmail(authentication.getName()));
        return Map.of("id", user.id(), "email", user.email(), "role", user.role().name(), "name", user.name());
    }

    @Operation(summary = "Redirecionar para confirmação no frontend", description = "Redireciona para a página React de confirmação de e-mail.", tags = "Usuários")
    @GetMapping("/api/users/verify-email")
    public ResponseEntity<Void> redirectToFrontendVerification(@RequestParam(required = false) String token) {
        String destination = properties.frontendUrl() + "/confirmar-email"
                + (token != null && !token.isBlank() ? "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8) : "");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(destination))
                .build();
    }
}
