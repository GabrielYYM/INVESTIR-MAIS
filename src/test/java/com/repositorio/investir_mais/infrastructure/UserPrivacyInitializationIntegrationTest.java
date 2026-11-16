package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.DriverManager;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=update")
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UserPrivacyInitializationIntegrationTest extends PostgreSqlIntegrationTest {
    @Autowired UserService accounts;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;
    @Autowired @Qualifier("initializeUserPrivacyColumns") SmartInitializingSingleton initialize;

    @BeforeAll static void legacySchema() throws Exception {
        try (var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
             var statement=connection.createStatement()) {
            statement.execute("CREATE TABLE tb_user (id uuid PRIMARY KEY, name varchar(255) NOT NULL, age integer NOT NULL, email varchar(255) NOT NULL UNIQUE, password varchar(255) NOT NULL, created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, email_verified boolean NOT NULL DEFAULT false)");
            statement.execute("INSERT INTO tb_user (id,name,age,email,password) VALUES ('00000000-0000-0000-0000-000000000001','Registro preservado',25,'legacy@example.test','legacy-hash')");
        }
    }

    @Test void registrationAndErasureWorkWithLegacyColumnsAndPreserveExistingUsers() {
        initialize.afterSingletonsInstantiated();
        String email="privacy-regression@example.test";
        accounts.registerUser(new UserDTO.Request("Teste privacidade",LocalDate.of(2000,1,1),email,"Temporary123!",UserRole.STUDENT));
        var account=users.findByEmail(email).orElseThrow();
        assertEquals(LocalDate.of(2000,1,1),account.getBirthDate());
        accounts.deleteAccount(email,account.getId(),new UserDTO.DeleteAccountRequest("Temporary123!"));
        assertNull(jdbc.queryForObject("SELECT name FROM tb_user WHERE id=?",String.class,account.getId()));
        assertNull(jdbc.queryForObject("SELECT age FROM tb_user WHERE id=?",Integer.class,account.getId()));
        assertNull(jdbc.queryForObject("SELECT password FROM tb_user WHERE id=?",String.class,account.getId()));
        assertEquals("Registro preservado",jdbc.queryForObject("SELECT name FROM tb_user WHERE email='legacy@example.test'",String.class));
        assertEquals(25,jdbc.queryForObject("SELECT age FROM tb_user WHERE email='legacy@example.test'",Integer.class));
    }
}
