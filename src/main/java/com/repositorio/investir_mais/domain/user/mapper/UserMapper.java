package com.repositorio.investir_mais.domain.user.mapper;

import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "userSecurity.email", source = "email")
    @Mapping(target = "userSecurity.password", source = "password")
    @Mapping(target = "userSecurity.role", source = "role")
    @Mapping(target = "userSecurity.emailVerified", constant = "false")
    User toEntity(UserDTO.Request dto);

    @Mapping(target = "email", source = "userSecurity.email")
    @Mapping(target = "role", source = "userSecurity.role")
    @Mapping(target = "emailVerified", source = "userSecurity.emailVerified")
    UserDTO.Response toDTO(User user);
}