package com.repositorio.investir_mais.domain.user.model;

import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Range;

import lombok.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.NotAudited;
import org.hibernate.jdbc.Expectation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@NamedInterface("user-model")
@Entity
@Table(name = "TB_USER")
@SQLDelete(sql = "UPDATE tb_user SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND deleted_at IS NULL", verify = Expectation.RowCount.class)
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
@AuditTable(value = "TB_USER_AUDIT")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String name;

    @Range(min = 0, max = 120)
    private Integer age;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotAudited
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean emailVerified;

    @Column(name = "deleted_at", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime deletedAt;

    @NotAudited
    @OneToMany(mappedBy = "professor", cascade = CascadeType.REMOVE)
    private List<Course> courses = new ArrayList<>();

    @NotAudited
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ElementCollection
    @CollectionTable(name = "TB_COURSE_ENROLLMENT", joinColumns = @JoinColumn(name = "student_id"))
    @MapKeyJoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<Course, Enrollment> enrollments = new HashMap<>();

    @NotAudited
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ElementCollection
    @CollectionTable(name = "TB_LESSON_PROGRESS", joinColumns = @JoinColumn(name = "student_id"))
    @MapKeyJoinColumn(name = "lesson_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<Lesson, Completion> lessonProgress = new HashMap<>();

    @NotAudited
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ElementCollection
    @CollectionTable(name = "TB_QUESTION_PASS", joinColumns = @JoinColumn(name = "student_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "question_id"}))
    @Column(name = "question_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<UUID> passedQuestionIds = new HashSet<>();

    public Set<UUID> getPassedQuestionIds() { return Set.copyOf(passedQuestionIds); }

    public void passQuestion(UUID questionId) { passedQuestionIds.add(questionId); }

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    public boolean enrollIn(Course course) {
        return enrollments.putIfAbsent(course, new Enrollment(UUID.randomUUID(), LocalDateTime.now())) == null;
    }

    public boolean isEnrolledIn(Course course) {
        return enrollments.containsKey(course);
    }

    public void setLessonCompleted(Lesson lesson, boolean completed) {
        Completion previous = lessonProgress.get(lesson);
        UUID id = previous == null ? UUID.randomUUID() : previous.id();
        LocalDateTime completedAt = previous == null ? null : previous.completedAt();
        if (completed && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
        lessonProgress.put(lesson, new Completion(id, completed, completedAt));
    }

    @Embeddable
    public record Enrollment(
            @Column(nullable = false, updatable = false) UUID id,
            @Column(name = "enrolled_at", nullable = false) LocalDateTime enrolledAt
    ) {}

    @Embeddable
    public record Completion(
            @Column(nullable = false, updatable = false) UUID id,
            @Column(nullable = false) boolean completed,
            @Column(name = "completed_at") LocalDateTime completedAt
    ) {}
}
