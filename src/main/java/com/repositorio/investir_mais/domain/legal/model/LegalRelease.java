package com.repositorio.investir_mais.domain.legal.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import org.hibernate.annotations.Immutable;

@Entity @Table(name="legal_document_release") @Immutable
@org.hibernate.annotations.Check(constraints="version > 0")
@Getter @NoArgsConstructor(access=AccessLevel.PROTECTED)
public class LegalRelease {
    @Id private int version;
    @Column(name="published_on", nullable=false) private LocalDate publishedOn;
    @Column(nullable=false, columnDefinition="text") private String content;
    @Column(name="content_hash", nullable=false, length=64) private String contentHash;
}
