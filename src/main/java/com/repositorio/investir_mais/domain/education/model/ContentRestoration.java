package com.repositorio.investir_mais.domain.education.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import java.time.Instant;
import java.util.UUID;

/** Append-only evidence of an administrative restore, including its original deletion date. */
@Entity @Table(name = "tb_content_restoration") @Audited
@Getter @NoArgsConstructor
public class ContentRestoration {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false, updatable = false) private String entityType;
    @Column(nullable = false, updatable = false) private UUID entityId;
    @Column(nullable = false, updatable = false) private UUID administratorId;
    @Column(nullable = false, updatable = false) private Instant deletedAt;
    @Column(nullable = false, updatable = false) private Instant restoredAt;
    public ContentRestoration(String entityType, UUID entityId, UUID administratorId, Instant deletedAt) {
        this.entityType = entityType; this.entityId = entityId; this.administratorId = administratorId;
        this.deletedAt = deletedAt; this.restoredAt = Instant.now();
    }
}
