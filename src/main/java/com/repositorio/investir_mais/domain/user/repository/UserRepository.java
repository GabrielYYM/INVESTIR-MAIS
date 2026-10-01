package com.repositorio.investir_mais.domain.user.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import com.repositorio.investir_mais.domain.user.model.User;

public interface UserRepository extends JpaRepository<User, UUID>, RevisionRepository<User, UUID, Integer> {
    User findByUserSecurityEmail(String email);
}