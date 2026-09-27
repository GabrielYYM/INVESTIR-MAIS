package com.repositorio.investir_mais.domain.education.mapper;

import com.repositorio.investir_mais.domain.education.DTO.ModuleRequest;
import com.repositorio.investir_mais.domain.education.DTO.ModuleResponse;
import com.repositorio.investir_mais.domain.education.model.Module;
import com.repositorio.investir_mais.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class ModuleMapper {

    public Module toEntity(ModuleRequest dto, User professor) {
        return new Module(dto.name(), dto.description(), dto.orderIndex(), professor);
    }

    /**
     * contentCount é calculado fora da entidade (no Query service, via
     * repository) e passado aqui só para montar o DTO — Module não conhece
     * seus próprios conteúdos (ver nota em ModuleRepository).
     */
    public ModuleResponse toResponse(Module entity, long contentCount) {
        return new ModuleResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getOrderIndex(),
                entity.getProfessor().getId(),
                entity.getProfessor().getName(),
                contentCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
