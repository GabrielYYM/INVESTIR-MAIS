package com.repositorio.investir_mais.domain.user.repository;

import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NamedInterface("user-repository")
public interface UserRepository extends JpaRepository<User, UUID>, RevisionRepository<User, UUID, Integer> {
    Optional<User> findByEmail(String email);

    Optional<UserDTO.Response> findResponseByEmail(String email);

    List<UserDTO.Response> findAllProjectedBy();

    @PreAuthorize("isAuthenticated() and #email == authentication.name")
    @PostAuthorize("returnObject.isEmpty() or returnObject.get().id == #requestedId")
    default Optional<UserDTO.Response> findOwnResponse(String email, UUID requestedId) {
        return findResponseByEmail(email);
    }

    // Called within the service transaction so the lock lasts through the account change.
    @PreAuthorize("isAuthenticated() and #email == authentication.name")
    @PostAuthorize("returnObject.isEmpty() or returnObject.get().id == #requestedId")
    default Optional<User> findOwnForUpdate(String email, UUID requestedId) {
        return findForUpdateByEmail(email);
    }


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findForUpdateByEmail(String email);


    boolean existsByEmail(String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_user WHERE email = :email)", nativeQuery = true)
    boolean existsByEmailIncludingDeleted(@Param("email") String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_user WHERE id = :id)", nativeQuery = true)
    boolean existsByIdIncludingDeleted(@Param("id") UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_user WHERE email = :email AND id <> :id)", nativeQuery = true)
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);
}
