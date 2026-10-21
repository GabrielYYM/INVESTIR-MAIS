package com.repositorio.investir_mais.domain.education.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "TB_LESSON")
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

    @Column(nullable = false)
    private String description;

    @URL
    @Column(nullable = false)
    private String mediaUrl;

    @Column(nullable = false)
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}