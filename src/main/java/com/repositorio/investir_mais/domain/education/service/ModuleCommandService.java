package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.ModuleRequest;
import com.repositorio.investir_mais.domain.education.DTO.ModuleResponse;
import com.repositorio.investir_mais.domain.education.mapper.ModuleMapper;
import com.repositorio.investir_mais.domain.education.model.Module;
import com.repositorio.investir_mais.domain.education.repository.EducationalContentRepository;
import com.repositorio.investir_mais.domain.education.repository.ModuleRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ModuleCommandService {

    private final ModuleRepository moduleRepository;
    private final EducationalContentRepository contentRepository;
    private final ModuleMapper mapper;

    public ModuleCommandService(
            ModuleRepository moduleRepository,
            EducationalContentRepository contentRepository,
            ModuleMapper mapper
    ) {
        this.moduleRepository = moduleRepository;
        this.contentRepository = contentRepository;
        this.mapper = mapper;
    }

    /**
     * Cria um módulo. Usado tanto pela tela de gestão de módulos quanto
     * pela opção "criar novo módulo" no dropdown da tela de Detalhes do vídeo.
     */
    @Transactional
    public ModuleResponse create(ModuleRequest request, User professor) {
        Module module = mapper.toEntity(request, professor);
        moduleRepository.save(module);
        return mapper.toResponse(module, 0);
    }

    @Transactional
    public ModuleResponse update(UUID moduleId, ModuleRequest request, User professor) {
        Module module = getOwnedModuleOrThrow(moduleId, professor);
        module.setName(request.name());
        module.setDescription(request.description());
        module.setOrderIndex(request.orderIndex());

        long contentCount = contentRepository.findAllByProfessorId(professor.getId()).stream()
                .filter(c -> c.getModule() != null && c.getModule().getId().equals(moduleId))
                .count();

        return mapper.toResponse(module, contentCount);
    }

    /**
     * Exclui o módulo. Não deleta os vídeos associados — eles voltam a
     * ficar "sem módulo" (visíveis só na listagem do professor), seguindo
     * a mesma regra de um vídeo criado sem módulo desde o início.
     */
    @Transactional
    public void delete(UUID moduleId, User professor) {
        Module module = getOwnedModuleOrThrow(moduleId, professor);

        contentRepository.findAllByProfessorId(professor.getId()).stream()
                .filter(c -> c.getModule() != null && c.getModule().getId().equals(moduleId))
                .forEach(c -> c.removeFromModule());

        moduleRepository.delete(module);
    }

    private Module getOwnedModuleOrThrow(UUID moduleId, User professor) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Módulo não encontrado"));

        if (!module.getProfessor().getId().equals(professor.getId())) {
            throw new AccessDeniedException("Você não tem permissão sobre este módulo");
        }
        return module;
    }
}