package com.repositorio.investir_mais.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.repositorio.investir_mais.domain.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
}
