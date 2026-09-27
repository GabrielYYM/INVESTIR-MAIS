package com.repositorio.investir_mais.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getSecurity() != null && user.getSecurity().getRole() != null ? user.getSecurity().getRole().name() : null)")
    UserResponseDTO toUserResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "security", ignore = true)
    User toUser(UserRequestDTO userRequestDTO);
}