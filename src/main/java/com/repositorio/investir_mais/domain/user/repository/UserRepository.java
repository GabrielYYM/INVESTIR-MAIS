package com.repositorio.investir_mais.domain.user.repository;

import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.modulith.NamedInterface;

import java.util.Optional;
import java.util.UUID;

@NamedInterface("user-repository")
public interface UserRepository extends JpaRepository<User, UUID>, RevisionRepository<User, UUID, Integer> {
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);


    boolean existsByEmail(String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_user WHERE email = :email)", nativeQuery = true)
    boolean existsByEmailIncludingDeleted(@Param("email") String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_user WHERE email = :email AND id <> :id)", nativeQuery = true)
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);
}
