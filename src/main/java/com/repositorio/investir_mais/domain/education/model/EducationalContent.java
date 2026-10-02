package com.repositorio.investir_mais.domain.education.model;

import com.repositorio.investir_mais.domain.education.model.enums.ContentStatus;
import com.repositorio.investir_mais.domain.education.model.enums.ContentType;
import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conteúdo educacional (vídeo ou artigo) da plataforma.
 *
 * Regras de negócio já definidas:
 * - Criado como DRAFT assim que o upload termina no Cloudinary; só fica
 *   visível ao aluno quando o professor publica manualmente (status = PUBLISHED).
 * - Sem módulo vinculado: aparece apenas na listagem do próprio professor,
 *   nunca é visível ao aluno (mesmo que PUBLISHED).
 * - Deleção é física (registro + arquivo no Cloudinary), diferente da
 *   deleção lógica (soft delete) usada para dados de usuário.
 */
@Entity
@Table(name = "educational_content")
public class EducationalContent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status;

    /**
     * URL do arquivo (vídeo ou artigo) retornada pelo Cloudinary.
     * Nunca armazenamos o binário no banco.
     */
    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    /**
     * URL da miniatura. Pode ser a gerada automaticamente pelo Cloudinary
     * ou uma customizada enviada pelo professor na tela de Detalhes.
     */
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    /**
     * Módulo (playlist) ao qual este conteúdo pertence. Opcional:
     * quando nulo, o conteúdo só aparece na listagem do professor.
     */
    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    /**
     * Ordem de exibição dentro do módulo (para a playlist do player).
     * Só tem sentido quando module != null.
     */
    @Column(name = "order_in_module")
    private Integer orderInModule;

    /**
     * Professor autor do conteúdo (quem fez o upload).
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected EducationalContent() {
        // exigido pelo JPA
    }

    /**
     * Uso no momento em que o upload termina no Cloudinary: cria o registro
     * já como DRAFT, sem módulo/detalhes ainda preenchidos.
     */
    public EducationalContent(String title, ContentType type, String mediaUrl, User professor) {
        this.title = title;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.professor = professor;
        this.status = ContentStatus.DRAFT;
    }

    public void publish() {
        this.status = ContentStatus.PUBLISHED;
    }

    public void unpublish() {
        this.status = ContentStatus.DRAFT;
    }

    public void assignToModule(Module module, Integer orderInModule) {
        this.module = module;
        this.orderInModule = orderInModule;
    }

    public void removeFromModule() {
        this.module = null;
        this.orderInModule = null;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ContentType getType() {
        return type;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Module getModule() {
        return module;
    }

    public Integer getOrderInModule() {
        return orderInModule;
    }

    public User getProfessor() {
        return professor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}