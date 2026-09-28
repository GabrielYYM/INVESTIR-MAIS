package com.repositorio.investir_mais.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getSecurity() != null && user.getSecurity().getRole() != null ? user.getSecurity().getRole().name() : null)")
    @Mapping(target = "requiresGuardianVerification", expression = "java(user.isUnder12())")
    UserResponseDTO toUserResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "security", ignore = true)
    @Mapping(target = "portfolio", ignore = true)
    User toUser(UserRequestDTO userRequestDTO);
}