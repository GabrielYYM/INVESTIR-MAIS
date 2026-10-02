package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.ModuleResponse;
import com.repositorio.investir_mais.domain.education.mapper.ModuleMapper;
import com.repositorio.investir_mais.domain.education.model.Module;
import com.repositorio.investir_mais.domain.education.model.enums.ContentStatus;
import com.repositorio.investir_mais.domain.education.repository.EducationalContentRepository;
import com.repositorio.investir_mais.domain.education.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ModuleQueryService {

    private final ModuleRepository moduleRepository;
    private final EducationalContentRepository contentRepository;
    private final ModuleMapper mapper;

    public ModuleQueryService(
            ModuleRepository moduleRepository,
            EducationalContentRepository contentRepository,
            ModuleMapper mapper
    ) {
        this.moduleRepository = moduleRepository;
        this.contentRepository = contentRepository;
        this.mapper = mapper;
    }

    /**
     * Tela de gestão/organização de módulos e dropdown "Adicionar ao módulo".
     * contentCount é calculado por módulo para exibir "8 vídeos" na listagem.
     */
    public List<ModuleResponse> listByProfessor(UUID professorId) {
        List<Module> modules = moduleRepository.findAllByProfessorIdOrderByOrderIndexAsc(professorId);
        var contents = contentRepository.findAllByProfessorId(professorId);

        return modules.stream()
                .map(module -> {
                    long count = contents.stream()
                            .filter(c -> c.getModule() != null && c.getModule().getId().equals(module.getId()))
                            .count();
                    return mapper.toResponse(module, count);
                })
                .toList();
    }

    /**
     * Listagem visível ao aluno: só módulos com pelo menos um conteúdo
     * PUBLISHED. contentCount aqui reflete só os PUBLISHED, diferente da
     * contagem total usada na tela do professor.
     */
    public List<ModuleResponse> listVisibleToStudents() {
        return moduleRepository.findModulesWithContentStatus(ContentStatus.PUBLISHED).stream()
                .map(module -> {
                    long publishedCount = contentRepository
                            .findAllByModuleIdAndStatusOrderByOrderInModuleAsc(module.getId(), ContentStatus.PUBLISHED)
                            .size();
                    return mapper.toResponse(module, publishedCount);
                })
                .toList();
    }
}