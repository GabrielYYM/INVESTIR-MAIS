package com.repositorio.investir_mais.domain.user.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.repositorio.investir_mais.common.model.Auditable;
import com.repositorio.investir_mais.domain.portfolio.model.Portfolio;
import com.repositorio.investir_mais.infrastructure.util.AttributeEncryptor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "TB_USER")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @ToString.Include
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 255)
    @Convert(converter = AttributeEncryptor.class)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 500)
    @Convert(converter = AttributeEncryptor.class)
    private String email;

    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    @Column(name = "guardian_email", length = 500)
    @Convert(converter = AttributeEncryptor.class)
    private String guardianEmail;

    @Column(name = "terms_accepted", nullable = false)
    @Builder.Default
    private Boolean termsAccepted = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Portfolio portfolio;

    @Embedded
    private UserSecurity security;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isUnder12() {
        if (this.birthDate == null) {
            return false;
        }
        return java.time.Period.between(this.birthDate, java.time.LocalDate.now()).getYears() < 12;
    }

    public void updateProfile(String name, String email, String emailHash) {
        this.name = name;
        this.email = email;
        if (this.security != null) {
            this.security.setEmailHash(emailHash);
        }
    }
}
