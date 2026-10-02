package com.repositorio.investir_mais.domain.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.repositorio.investir_mais.domain.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsBySecurityEmailHash(String emailHash);

    Optional<User> findBySecurityEmailHash(String emailHash);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :threshold")
    List<User> findAllPendingPurge(@Param("threshold") LocalDateTime threshold);
}
