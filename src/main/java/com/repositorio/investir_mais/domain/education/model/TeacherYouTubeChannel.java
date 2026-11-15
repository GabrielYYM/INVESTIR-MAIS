package com.repositorio.investir_mais.domain.education.model;

import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_teacher_youtube_channel", uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "channel_id"}))
@Getter @Setter @NoArgsConstructor
public class TeacherYouTubeChannel {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "teacher_id", nullable = false) private User teacher;
    @Column(name = "channel_id", nullable = false, length = 24) private String channelId;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private UUID approvedBy;
    @Column(nullable = false) private Instant approvedAt;
}
