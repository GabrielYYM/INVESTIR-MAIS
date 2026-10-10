package com.repositorio.investir_mais.domain.user.mapper;

import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "emailVerified", constant = "false")
    User toEntity(UserDTO.Request dto);

    UserDTO.Response toDTO(User user);
}