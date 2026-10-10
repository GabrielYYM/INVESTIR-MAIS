package com.repositorio.investir_mais.domain.user.repository;

import com.repositorio.investir_mais.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, RevisionRepository<User, UUID, Integer> {
    Optional<User> findByEmail(String email);
}