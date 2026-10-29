package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/auth/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        var user = userMapper.toDTO(userService.findByEmail(authentication.getName()));
        return Map.of("id", user.id(), "email", user.email(), "role", user.role().name(), "name", user.name());
    }
}
