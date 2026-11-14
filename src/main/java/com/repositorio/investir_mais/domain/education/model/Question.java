package com.repositorio.investir_mais.domain.education.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_question")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Getter 
@Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @Column(nullable = false, length = 5000)
    private String prompt;
    @Column(nullable = false)
    private int position;
    // Number of the lesson after which this question appears; zero means before the first lesson.
    @Column(nullable = false)
    private int afterLesson;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(nullable = false, length = 2000)
    private List<String> options = new ArrayList<>();
    @Column(nullable = false)
    private int correctOptionIndex;
}
