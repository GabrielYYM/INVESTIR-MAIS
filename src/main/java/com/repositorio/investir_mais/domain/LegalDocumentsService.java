package com.repositorio.investir_mais.domain;

import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Published releases are immutable; the highest persisted version remains current across rolling deployments. */
@Service
public class LegalDocumentsService {
    public record Section(String heading, String content) {}
    public record Document(String id, String label, String title, List<Section> sections) {}
    public record Release(int version, LocalDate publishedOn, List<Document> documents) {}
    public record Status(int currentVersion, boolean acceptanceRequired, Instant acceptedAt) {}
    public record Acceptance(int version, Instant acceptedAt) {}

    private final JdbcClient jdbc;
    private final UserRepository users;
    private final ObjectMapper json;

    public LegalDocumentsService(JdbcClient jdbc, UserRepository users, ObjectMapper json) {
        this.jdbc = jdbc;
        this.users = users;
        this.json = json;
    }

    @Transactional
    public void publishBundledReleases() {
        try {
            var resources = new PathMatchingResourcePatternResolver().getResources("classpath*:legal/v*.json");
            if (resources.length == 0) throw new IllegalStateException("Nenhum documento legal publicado.");
            // Validate every release before registering any of them.
            var releases = new java.util.ArrayList<Release>();
            for (var resource : resources) {
                releases.add(json.readValue(resource.getContentAsString(StandardCharsets.UTF_8), Release.class));
            }
            releases.sort(Comparator.comparingInt(Release::version));
            if (releases.stream().map(Release::version).distinct().count() != releases.size()) {
                throw new IllegalStateException("Versões legais duplicadas.");
            }
            releases.forEach(this::validate);
            releases.forEach(this::publish);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível carregar os documentos legais.", exception);
        }
    }

    private void validate(Release release) {
        if (release.version() < 1 || release.publishedOn() == null || release.publishedOn().isAfter(LocalDate.now())
                || release.documents() == null || release.documents().size() != 2
                || !release.documents().stream().map(Document::id).collect(java.util.stream.Collectors.toSet())
                    .equals(Set.of("termos", "privacidade"))) {
            throw new IllegalStateException("Versão legal inválida: " + release.version());
        }
        for (var document : release.documents()) {
            if (document.label() == null || document.label().isBlank() || document.title() == null || document.title().isBlank()
                    || document.sections() == null || document.sections().isEmpty()
                    || document.sections().stream().anyMatch(section -> section.heading() == null || section.heading().isBlank()
                        || section.content() == null || section.content().isBlank())) {
                throw new IllegalStateException("Documento legal incompleto: " + document.id());
            }
        }
    }

    private void publish(Release release) {
        String content = json.writeValueAsString(release);
        String hash = DigestUtils.sha256Hex(content);
        jdbc.sql("""
            INSERT INTO legal_document_release (version, published_on, content, content_hash)
            VALUES (?, ?, ?, ?) ON CONFLICT (version) DO NOTHING
            """).params(release.version(), release.publishedOn(), content, hash).update();
        String persistedHash = jdbc.sql("SELECT content_hash FROM legal_document_release WHERE version = ?")
                .param(release.version()).query(String.class).single();
        if (!hash.equals(persistedHash)) {
            throw new IllegalStateException("A versão legal " + release.version() + " já foi publicada. Crie uma nova versão.");
        }
    }

    @Transactional(readOnly = true)
    public Release current() {
        return json.readValue(jdbc.sql("SELECT content FROM legal_document_release ORDER BY version DESC LIMIT 1")
                .query(String.class).single(), Release.class);
    }

    @Transactional(readOnly = true)
    public Release release(int version) {
        String content = jdbc.sql("SELECT content FROM legal_document_release WHERE version = ?").param(version)
                .query(String.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versão não encontrada."));
        return json.readValue(content, Release.class);
    }

    @Transactional(readOnly = true)
    public Status status(String email) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        int currentVersion = jdbc.sql("SELECT MAX(version) FROM legal_document_release").query(Integer.class).single();
        var acceptedAt = jdbc.sql("SELECT accepted_at FROM legal_document_acceptance WHERE user_id = ? AND version = ?")
                .params(user.getId(), currentVersion).query(Instant.class).optional();
        return new Status(currentVersion, acceptedAt.isEmpty(), acceptedAt.orElse(null));
    }

    @Transactional
    public Status accept(String email, int version) {
        // Serialize repeated submissions for the same account; the unique key also makes retries idempotent.
        var user = users.findForUpdateByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (version != current().version()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Os documentos foram atualizados. Leia a nova versão antes de confirmar.");
        }
        jdbc.sql("""
            INSERT INTO legal_document_acceptance (user_id, version, accepted_at)
            VALUES (?, ?, CURRENT_TIMESTAMP) ON CONFLICT (user_id, version) DO NOTHING
            """).params(user.getId(), version).update();
        return status(email);
    }

    @Transactional(readOnly = true)
    public List<Acceptance> history(String email) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return jdbc.sql("SELECT version, accepted_at FROM legal_document_acceptance WHERE user_id = ? ORDER BY version DESC")
                .param(user.getId()).query((rs, row) -> new Acceptance(rs.getInt("version"), rs.getTimestamp("accepted_at").toInstant())).list();
    }
}
