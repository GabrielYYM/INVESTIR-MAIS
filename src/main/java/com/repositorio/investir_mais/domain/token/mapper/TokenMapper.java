package com.repositorio.investir_mais.domain.token.mapper;

import com.repositorio.investir_mais.domain.token.DTO.TokenDTO;
import com.repositorio.investir_mais.domain.token.model.AccountToken;
import com.repositorio.investir_mais.domain.token.model.LoginToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapstruct.*;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.OneTimeToken;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TokenMapper {
    @Mapping(target = "tokenValue", source = "token.tokenValue", qualifiedByName = "hash")
    AccountToken toAccountEntity(TokenDTO.Issued token, String purpose);

    LoginToken toLoginEntity(TokenDTO.Issued token);
    TokenDTO.Issued toDTO(LoginToken token);

    default OneTimeToken toSecurityToken(TokenDTO.Issued token) {
        return new DefaultOneTimeToken(token.tokenValue(), token.username(), token.expiresAt());
    }

    @Named("hash")
    default String hash(String value) { return DigestUtils.sha256Hex(value); }
}
