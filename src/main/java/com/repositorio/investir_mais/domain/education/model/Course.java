package com.repositorio.investir_mais.domain.education.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Table(name = "TB_COURSE")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Course {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @org.hibernate.annotations.ColumnDefault("'Geral'")
    @Column(nullable = false, length = 100)
    private String category = "Geral";

    @Column(length = 1000)
    private String keywords = "";

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;

    @NotAudited
    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    @OrderBy("position ASC, id ASC")
    private List<Lesson> lessons = new ArrayList<>();

    @NotAudited
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @OrderBy("afterLesson ASC, position ASC, id ASC")
    private List<Question> questions = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
