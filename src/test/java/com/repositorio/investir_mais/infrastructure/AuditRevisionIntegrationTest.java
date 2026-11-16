package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AuditRevisionIntegrationTest extends PostgreSqlIntegrationTest {
    @Autowired UserRepository users;
    @Autowired UserService service;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void profileUpdatesWorkAcrossMultipleRevisionAllocations() {
        User account = new User();
        account.setName("Conta de regressão");
        account.setAge(25);
        account.setEmail("audit-regression@investir.test");
        account.setRole(UserRole.STUDENT);
        account.setEmailVerified(true);
        account.setPassword(encoder.encode("SenhaTeste123"));
        var saved = users.saveAndFlush(account);

        var birthDate = LocalDate.of(2000, 1, 15);
        var updated = service.updateProfile(account.getEmail(), saved.getId(),
                new UserDTO.UpdateProfileRequest("Nome atualizado", account.getEmail(), birthDate, "SenhaTeste123"));
        assertEquals("Nome atualizado", updated.name());
        assertEquals(birthDate, users.findById(saved.getId()).orElseThrow().getBirthDate());

        for (int i = 0; i < 55; i++) {
            int revision = i;
            new TransactionTemplate(transactions).executeWithoutResult(status ->
                    users.findById(saved.getId()).orElseThrow().setName("Revisão " + revision));
        }
        assertEquals(57, jdbc.queryForObject("SELECT COUNT(*) FROM tb_user_audit WHERE id = ?", Integer.class, saved.getId()));
        assertTrue(jdbc.queryForObject("SELECT MIN(rev) FROM tb_user_audit WHERE id = ?", Integer.class, saved.getId()) > 0);
    }
}
