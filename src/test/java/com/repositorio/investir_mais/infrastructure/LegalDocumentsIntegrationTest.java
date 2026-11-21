package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import com.repositorio.investir_mais.domain.legal.DTO.LegalDocumentDTO;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class LegalDocumentsIntegrationTest extends PostgreSqlIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired LegalDocumentsService legal;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    User account;

    @BeforeEach
    void createAccount() {
        account = new User();
        account.setName("Teste documentos");
        account.setEmail(UUID.randomUUID() + "@example.test");
        account.setRole(UserRole.STUDENT);
        account.setPassword(encoder.encode("Temporary-test-password"));
        account.setEmailVerified(true);
        users.saveAndFlush(account);
    }

    private RequestPostProcessor signedIn() {
        return user(account.getEmail()).authorities(new SimpleGrantedAuthority("ROLE_STUDENT"),
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY),
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.OTT_AUTHORITY));
    }

    @Test
    void existingAccountsMustExplicitlyConfirmBeforeAccessingBusinessEndpoints() throws Exception {
        mvc.perform(get("/api/legal/current")).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.version").value(1));
        mvc.perform(get("/api/legal/status")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/courses").with(signedIn())).andExpect(status().isPreconditionRequired())
                .andExpect(jsonPath("$.code").value("LEGAL_ACCEPTANCE_REQUIRED"));
        mvc.perform(get("/api/legal/status").with(signedIn())).andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptanceRequired").value(true));
        mvc.perform(get("/api/users/{id}", account.getId()).with(signedIn())).andExpect(status().isOk());
        for (String body : new String[]{"{\"version\":1}", "{\"version\":1,\"accepted\":false}"}) {
            mvc.perform(post("/api/legal/acceptances").with(signedIn()).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/legal/acceptances").with(signedIn()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1,\"accepted\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.acceptanceRequired").value(false));
        mvc.perform(get("/api/courses").with(signedIn())).andExpect(status().isOk());
        var acceptedAt = legal.status(account.getEmail()).acceptedAt();
        legal.accept(account.getEmail(), 1);
        assertEquals(acceptedAt, legal.status(account.getEmail()).acceptedAt());
        assertEquals(1, legal.history(account.getEmail()).size());
    }

    @Test
    void newReleaseBlocksAnAlreadyAuthenticatedUserAndRejectsTheOldConfirmation() throws Exception {
        legal.accept(account.getEmail(), 1);
        var original = legal.current();
        var updated = new LegalDocumentDTO.Release(2, original.publishedOn(), original.documents());
        String content = json.writeValueAsString(updated);
        jdbc.update("INSERT INTO legal_document_release (version, published_on, content, content_hash) VALUES (?, ?, ?, ?)", 2, updated.publishedOn(), content,
                DigestUtils.sha256Hex(content));
        legal.publishBundledReleases();
        assertEquals(2, legal.current().version());
        mvc.perform(get("/api/courses").with(signedIn())).andExpect(status().isPreconditionRequired());
        mvc.perform(post("/api/legal/acceptances").with(signedIn()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1,\"accepted\":true}"))
                .andExpect(status().isConflict());
        legal.accept(account.getEmail(), 2);
        assertFalse(legal.status(account.getEmail()).acceptanceRequired());
        assertEquals(2, legal.history(account.getEmail()).size());
        assertEquals(original, legal.release(1));
        mvc.perform(get("/api/legal/acceptances").with(signedIn())).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].version").value(2)).andExpect(jsonPath("$[1].version").value(1));
    }

    @Test
    void acceptanceRequiresBothAuthenticationFactorsAndCsrf() throws Exception {
        mvc.perform(post("/api/legal/acceptances").with(signedIn())
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1,\"accepted\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/legal/acceptances").with(csrf()).with(user(account.getEmail()).authorities(
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1,\"accepted\":true}"))
                .andExpect(status().isForbidden());
        assertTrue(legal.history(account.getEmail()).isEmpty());
    }

    @Test
    void aPendingAccountCanBeDeletedWithoutAcceptingTheDocuments() throws Exception {
        mvc.perform(delete("/api/users/{id}", account.getId()).with(signedIn()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"currentPassword\":\"Temporary-test-password\"}"))
                .andExpect(status().isNoContent());
        assertTrue(users.findByEmail(account.getEmail()).isEmpty());
    }

    @Test
    void acceptancesAreBoundToTheAuthenticatedAccount() throws Exception {
        var other = new User();
        other.setName("Outra conta");
        other.setEmail(UUID.randomUUID() + "@example.test");
        other.setPassword("unused");
        other.setRole(UserRole.STUDENT);
        users.saveAndFlush(other);
        mvc.perform(post("/api/legal/acceptances").with(signedIn()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1,\"accepted\":true,\"userId\":\"" + other.getId() + "\"}"))
                .andExpect(status().isOk());
        assertEquals(1, legal.history(account.getEmail()).size());
        assertTrue(legal.history(other.getEmail()).isEmpty());
        assertTrue(legal.status(other.getEmail()).acceptanceRequired());
    }

    @Test
    void aPublishedVersionWithDifferentContentCannotBeSilentlyReplaced() {

        jdbc.execute("ALTER TABLE legal_document_release DISABLE TRIGGER legal_release_immutable");
        jdbc.update("UPDATE legal_document_release SET content_hash = ? WHERE version = 1", "0".repeat(64));
        jdbc.execute("ALTER TABLE legal_document_release ENABLE TRIGGER legal_release_immutable");
        var failure = assertThrows(IllegalStateException.class, legal::publishBundledReleases);
        assertTrue(failure.getMessage().contains("Crie uma nova versão"));
    }

    @Test
    void databaseProtectsPublishedDocumentsAndAcceptanceEvidence() {
        legal.accept(account.getEmail(), 1);
        for (String table : new String[]{"legal_document_release", "legal_document_acceptance"}) {
            for (String statement : new String[]{"UPDATE " + table + " SET version = version", "DELETE FROM " + table, "TRUNCATE " + table + " CASCADE"}) {
                jdbc.execute("SAVEPOINT immutable_check");
                assertThrows(DataAccessException.class, () -> jdbc.execute(statement));
                jdbc.execute("ROLLBACK TO SAVEPOINT immutable_check");
            }
        }
        assertEquals(1, legal.history(account.getEmail()).size());
    }

    @Test
    void exportsOpenApiWhenRequested() throws Exception {
        String output = System.getProperty("account.openapi.output");
        var response = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse();
        if (output != null) Files.writeString(Path.of(output), response.getContentAsString(StandardCharsets.UTF_8));
    }
}
