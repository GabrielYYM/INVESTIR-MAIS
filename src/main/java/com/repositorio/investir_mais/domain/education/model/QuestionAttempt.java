package com.repositorio.investir_mais.domain.education.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "question_attempt", indexes = {@Index(columnList = "studentId,courseId"), @Index(columnList = "questionId")})
@Getter @Setter @NoArgsConstructor
public class QuestionAttempt {
    @Id @GeneratedValue 
    private UUID id;

    @Column(nullable = false) 
    private UUID studentId;

    @Column(nullable = false) 
    private UUID courseId;

    @Column(nullable = false) 
    private UUID questionId;

    @Column(nullable = false, length = 5000) 
    private String prompt;

    @Column(nullable = false, length = 2000) 
    private String selectedAnswer;

    @Column(nullable = false) 
    private boolean correct;

    @Column(nullable = false) 
    private Instant answeredAt;
}
