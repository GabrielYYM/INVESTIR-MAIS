package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.ContentResponse;
import com.repositorio.investir_mais.domain.education.mapper.EducationalContentMapper;
import com.repositorio.investir_mais.domain.education.model.EducationalContent;
import com.repositorio.investir_mais.domain.education.model.enums.ContentStatus;
import com.repositorio.investir_mais.domain.education.repository.EducationalContentRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EducationalContentQueryService {

    private final EducationalContentRepository contentRepository;
    private final EducationalContentMapper mapper;

    public EducationalContentQueryService(
            EducationalContentRepository contentRepository,
            EducationalContentMapper mapper
    ) {
        this.contentRepository = contentRepository;
        this.mapper = mapper;
    }

    /**
     * Tela de listagem "Conteúdo do Canal": tudo que o professor criou,
     * DRAFT ou PUBLISHED, com ou sem módulo.
     */
    public List<ContentResponse> listByProfessor(UUID professorId) {
        return contentRepository.findAllByProfessorId(professorId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Painel lateral do player (playlist do módulo): só conteúdo PUBLISHED,
     * na ordem definida pelo professor.
     */
    public List<ContentResponse> listPublishedByModule(UUID moduleId) {
        return contentRepository
                .findAllByModuleIdAndStatusOrderByOrderInModuleAsc(moduleId, ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public ContentResponse getById(UUID contentId, User user) {
        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Conteúdo não encontrado"));

        UserRole role = user.getSecurity().getRole();

        // Aluno nunca pode descobrir/abrir um DRAFT por UUID.
        if (role == UserRole.ALUNO && content.getStatus() != ContentStatus.PUBLISHED) {
            throw new AccessDeniedException("Conteúdo ainda não está publicado");
        }

        // Professor visualiza seus próprios conteúdos de gestão; ADMIN/OWNER
        // podem consultar conteúdos independentemente do autor.
        if (role == UserRole.PROFESSOR
                && !content.getProfessor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Você não tem permissão sobre este conteúdo");
        }

        return mapper.toResponse(content);
    }

    /**
     * Feed geral para a Home do aluno ("Descubra"/"Mais vistos"). Sem
     * conceito de "destaque" no modelo ainda — o front decide como separar
     * os primeiros N como destaque e o resto como grade.
     */
    public List<ContentResponse> listPublished() {
        return contentRepository.findAllByStatusOrderByCreatedAtDesc(ContentStatus.PUBLISHED).stream()
                .map(mapper::toResponse)
                .toList();
    }
}