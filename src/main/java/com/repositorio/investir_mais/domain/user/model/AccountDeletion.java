package com.repositorio.investir_mais.domain.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "account_deletion", indexes = @Index(columnList = "userId,status")) @Audited
@Getter @Setter @NoArgsConstructor
public class AccountDeletion {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private UUID userId;
    @Column(nullable = false) private Instant requestedAt;
    @Column(nullable = false) private Instant purgeAfter;
    @Column(nullable = false) private String status = "PENDING";
    @JdbcTypeCode(SqlTypes.ARRAY) @Column(nullable = false) private List<UUID> courseIds = new ArrayList<>();
    @JdbcTypeCode(SqlTypes.ARRAY) @Column(nullable = false) private List<UUID> lessonIds = new ArrayList<>();
    @JdbcTypeCode(SqlTypes.ARRAY) @Column(nullable = false) private List<UUID> questionIds = new ArrayList<>();
    private Instant restoredAt;
    private UUID restoredBy;
    @Column(length = 2000) private String restorationReason;
}
