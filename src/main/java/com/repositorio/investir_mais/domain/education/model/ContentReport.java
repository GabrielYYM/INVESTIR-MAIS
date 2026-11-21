package com.repositorio.investir_mais.domain.education.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_content_report", indexes = {
    @Index(name = "content_report_status_created", columnList = "status,createdAt"),
    @Index(name = "content_report_reporter", columnList = "reporterId")
})
@Audited
@Getter @Setter @NoArgsConstructor
public class ContentReport {
    public enum Target { LESSON, TEACHER }
    public enum Reason { INAPPROPRIATE, OFF_TOPIC, MISLEADING, HARASSMENT, COPYRIGHT, OTHER }
    public enum Status { OPEN, RESOLVED, DISMISSED }

    @Id @GeneratedValue private UUID id;
    @Column(nullable = false, updatable = false) private UUID reporterId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false) private Target targetType;
    @Column(nullable = false, updatable = false) private UUID targetId;
    @Column(nullable = false, updatable = false) private UUID courseId;
    @Column(nullable = false, updatable = false, length = 500) private String targetLabel;
    @Column(updatable = false, columnDefinition = "text") private String mediaUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false) private Reason reason;
    @Column(nullable = false, updatable = false, length = 2000) private String description;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status = Status.OPEN;
    @Column(length = 2000) private String reviewNote;
    private UUID reviewerId;
    private Instant reviewedAt;
}
