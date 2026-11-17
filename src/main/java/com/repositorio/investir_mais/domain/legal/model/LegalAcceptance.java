package com.repositorio.investir_mais.domain.legal.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name="legal_document_acceptance") 
@Immutable
@Getter 
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class LegalAcceptance {
    @Embeddable
    public record Id(@Column(name="user_id") UUID userId, int version) implements Serializable {}
    @EmbeddedId 
    private Id id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="version", insertable=false, updatable=false)
    private LegalRelease release;
    
    @org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name="accepted_at", nullable=false) private Instant acceptedAt;
    public LegalAcceptance(UUID userId, int version, Instant acceptedAt) {
        this.id = new Id(userId, version);
        this.acceptedAt = acceptedAt;
    }
}
