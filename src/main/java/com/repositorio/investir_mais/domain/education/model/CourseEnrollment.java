package com.repositorio.investir_mais.domain.education.model;

import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_COURSE_ENROLLMENT", uniqueConstraints = {
    @UniqueConstraint(name = "uk_enrollment_student_course", columnNames = {"student_id", "course_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @PrePersist
    public void prePersist() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
    }
}

