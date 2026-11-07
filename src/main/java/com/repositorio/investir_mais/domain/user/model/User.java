package com.repositorio.investir_mais.domain.user.model;

import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Range;

import lombok.*;

import org.hibernate.annotations.Audited;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.jdbc.Expectation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
@Audited.Table(name = "TB_USER_AUDIT")
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

    @Audited.Excluded
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean emailVerified;

    @Column(name = "deleted_at", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "professor", cascade = CascadeType.REMOVE)
    private List<Course> courses = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;
}
