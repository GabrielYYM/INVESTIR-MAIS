package com.repositorio.investir_mais.domain.education.mapper;

import com.repositorio.investir_mais.domain.education.DTO.ContentResponse;
import com.repositorio.investir_mais.domain.education.DTO.CreateContentRequest;
import com.repositorio.investir_mais.domain.education.model.EducationalContent;
import com.repositorio.investir_mais.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class EducationalContentMapper {

    /**
     * Usado pelo Command service ao criar o registro, logo após o
     * Cloudinary confirmar o upload (status nasce DRAFT dentro do
     * construtor da entidade).
     */
    public EducationalContent toEntity(CreateContentRequest dto, User professor) {
        return new EducationalContent(dto.title(), dto.type(), dto.mediaUrl(), professor);
    }

    /**
     * Usado pelo Query service para expor o conteúdo via API, tanto na
     * listagem do professor quanto na visão do aluno.
     */
    public ContentResponse toResponse(EducationalContent entity) {
        return new ContentResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getType(),
                entity.getStatus(),
                entity.getMediaUrl(),
                entity.getThumbnailUrl(),
                entity.getModule() != null ? entity.getModule().getId() : null,
                entity.getModule() != null ? entity.getModule().getName() : null,
                entity.getOrderInModule(),
                entity.getProfessor().getId(),
                entity.getProfessor().getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
