package com.repositorio.investir_mais.domain.user.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "teacher_application", uniqueConstraints = @UniqueConstraint(columnNames = "userId"))
@Getter @Setter @NoArgsConstructor
public class TeacherApplication {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private UUID userId;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private Instant submittedAt;
    private Instant reviewedAt;
    private UUID reviewedBy;
    @Column(length = 2000) private String reviewReason;
    @Version private long version;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "application_id", nullable = false)
    private List<TeacherDocument> documents = new ArrayList<>();
}
