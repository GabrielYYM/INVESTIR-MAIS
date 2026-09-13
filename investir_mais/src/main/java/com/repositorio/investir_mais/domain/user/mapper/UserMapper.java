package com.repositorio.investir_mais.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.repositorio.investir_mais.domain.user.DTO.UserRequestDTO;
import com.repositorio.investir_mais.domain.user.DTO.UserResponseDTO;
import com.repositorio.investir_mais.domain.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toUserResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    User toUser(UserRequestDTO userRequestDTO);
}
