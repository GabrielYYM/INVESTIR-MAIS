package com.repositorio.investir_mais.domain.audit.mapper;

import org.mapstruct.Mapper;

import com.repositorio.investir_mais.domain.audit.DTO.AuditLogResponseDTO;
import com.repositorio.investir_mais.domain.audit.model.AuditLog;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponseDTO toDTO(AuditLog auditLog);
}
