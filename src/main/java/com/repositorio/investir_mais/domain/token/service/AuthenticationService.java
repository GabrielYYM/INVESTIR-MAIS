package com.repositorio.investir_mais.domain.token.service;

import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.mapper.UserMapper;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService users;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public UserDTO.Response currentUser(String email) {
        return mapper.toDTO(users.findByEmail(email));
    }
}
