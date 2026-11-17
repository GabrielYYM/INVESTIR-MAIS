package com.repositorio.investir_mais.domain.education.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.validator.constraints.URL;

@Audited
@Entity
@Table(name = "TB_LESSON")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @URL
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mediaUrl;

    @URL
    @Column(nullable = false, columnDefinition = "TEXT")
    private String thumbnailUrl;

    @NotAudited
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int position;
}
