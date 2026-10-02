package com.repositorio.investir_mais.domain.education.model;

import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Módulo (playlist) de conteúdos educacionais, ex: "Educação Financeira - Básico".
 *
 * Relação 1 Professor : N Módulos — cada módulo pertence a um único professor.
 * Suporte a múltiplos professores por módulo fica como feature futura, fora do MVP.
 */
@Entity
@Table(name = "education_module")
public class Module {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    /**
     * Ordem de exibição do módulo na listagem do aluno.
     */
    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    /**
     * Professor dono do módulo. Só ele (ou Admin/Owner) pode editar/excluir
     * o módulo e adicionar conteúdos a ele.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Module() {
        // exigido pelo JPA
    }

    public Module(String name, String description, int orderIndex, User professor) {
        this.name = name;
        this.description = description;
        this.orderIndex = orderIndex;
        this.professor = professor;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
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