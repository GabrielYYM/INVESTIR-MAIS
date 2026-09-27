package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.ContentResponse;
import com.repositorio.investir_mais.domain.education.DTO.CreateContentRequest;
import com.repositorio.investir_mais.domain.education.DTO.UpdateContentRequest;
import com.repositorio.investir_mais.domain.education.mapper.EducationalContentMapper;
import com.repositorio.investir_mais.domain.education.model.EducationalContent;
import com.repositorio.investir_mais.domain.education.model.Module;
import com.repositorio.investir_mais.domain.education.repository.EducationalContentRepository;
import com.repositorio.investir_mais.domain.education.repository.ModuleRepository;
import com.repositorio.investir_mais.domain.education.service.interfaces.MediaStorageService;
import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class EducationalContentCommandService {

    private final EducationalContentRepository contentRepository;
    private final ModuleRepository moduleRepository;
    private final EducationalContentMapper mapper;
    private final MediaStorageService mediaStorageService;

    public EducationalContentCommandService(
            EducationalContentRepository contentRepository,
            ModuleRepository moduleRepository,
            EducationalContentMapper mapper,
            MediaStorageService mediaStorageService
    ) {
        this.contentRepository = contentRepository;
        this.moduleRepository = moduleRepository;
        this.mapper = mapper;
        this.mediaStorageService = mediaStorageService;
    }

    /**
     * Chamado assim que o front confirma que o upload no Cloudinary terminou.
     * Cria o registro já como DRAFT (regra fica dentro do construtor da entidade).
     */
    @Transactional
    public ContentResponse create(CreateContentRequest request, User professor) {
        EducationalContent content = mapper.toEntity(request, professor);
        contentRepository.save(content);
        return mapper.toResponse(content);
    }

    /**
     * Preenche/edita os detalhes (tela "Detalhes do vídeo"): título, descrição,
     * miniatura e módulo. Valida que o módulo informado pertence ao mesmo
     * professor autor do conteúdo — evita um professor vincular vídeo a
     * módulo de outro professor.
     */
    @Transactional
    public ContentResponse update(UUID contentId, UpdateContentRequest request, User professor) {
        EducationalContent content = getOwnedContentOrThrow(contentId, professor);

        content.setTitle(request.title());
        content.setDescription(request.description());
        if (request.thumbnailUrl() != null) {
            content.setThumbnailUrl(request.thumbnailUrl());
        }

        if (request.moduleId() != null) {
            Module module = moduleRepository.findById(request.moduleId())
                    .orElseThrow(() -> new EntityNotFoundException("Módulo não encontrado"));

            if (!module.getProfessor().getId().equals(professor.getId())) {
                throw new AccessDeniedException("Módulo pertence a outro professor");
            }

            content.assignToModule(module, request.orderInModule());
        } else {
            content.removeFromModule();
        }

        return mapper.toResponse(content);
    }

    /**
     * Ação explícita de publicar (botão "Publicar" na tela de Detalhes).
     * Antes de publicar, exige que o conteúdo tenha um título preenchido
     * (garantido pelo @NotBlank do DTO na criação) — aqui só troca o status.
     */
    @Transactional
    public ContentResponse publish(UUID contentId, User professor) {
        EducationalContent content = getOwnedContentOrThrow(contentId, professor);
        content.publish();
        return mapper.toResponse(content);
    }

    @Transactional
    public ContentResponse unpublish(UUID contentId, User professor) {
        EducationalContent content = getOwnedContentOrThrow(contentId, professor);
        content.unpublish();
        return mapper.toResponse(content);
    }

    /**
     * Deleção física: remove o registro do banco e o arquivo no Cloudinary.
     * Diferente da deleção de usuário (soft delete) — ver decisão registrada
     * na issue da feature.
     */
    @Transactional
    public void delete(UUID contentId, User professor) {
        EducationalContent content = getOwnedContentOrThrow(contentId, professor);
        mediaStorageService.deleteMedia(content.getMediaUrl());
        contentRepository.delete(content);
    }

    private EducationalContent getOwnedContentOrThrow(UUID contentId, User professor) {
        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Conteúdo não encontrado"));

        boolean isOwner = content.getProfessor().getId().equals(professor.getId());
        // TODO: liberar também para ADMIN/OWNER conforme a regra de authorization
        // que vocês definirem (ex: professor.hasRole(ADMIN) || professor.hasRole(OWNER))
        if (!isOwner) {
            throw new AccessDeniedException("Você não tem permissão sobre este conteúdo");
        }
        return content;
    }
}