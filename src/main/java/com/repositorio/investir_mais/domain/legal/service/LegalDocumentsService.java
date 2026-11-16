package com.repositorio.investir_mais.domain.legal.service;

import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.legal.model.*;
import com.repositorio.investir_mais.domain.legal.repository.*;
import com.repositorio.investir_mais.domain.legal.DTO.LegalDocumentDTO.*;
import com.repositorio.investir_mais.domain.legal.mapper.LegalDocumentMapper;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.modulith.NamedInterface("legal-service")
@Service
@RequiredArgsConstructor
public class LegalDocumentsService {
    private final JdbcClient jdbc;
    private final LegalReleaseRepository releases;
    private final LegalAcceptanceRepository acceptances;
    private final UserRepository users;
    private final LegalDocumentMapper mapper;

    @Transactional
    public void publishBundledReleases() {
        try {
            var resources = new PathMatchingResourcePatternResolver().getResources("classpath*:legal/v*.json");
            if (resources.length == 0) throw new IllegalStateException("Nenhum documento legal publicado.");
            var releases = new ArrayList<Release>();
            for (var resource : resources) {
                releases.add(mapper.fromContent(resource.getContentAsString(StandardCharsets.UTF_8)));
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
                || release.documents() == null || release.documents().size() < 2 || release.documents().size() > 20
                || release.documents().stream().anyMatch(d -> d == null || d.id() == null || !d.id().matches("[a-z][a-z0-9-]{0,63}"))
                || release.documents().stream().map(Document::id).distinct().count() != release.documents().size()
                || !release.documents().stream().map(Document::id).collect(Collectors.toSet())
                    .containsAll(Set.of("termos", "privacidade"))) {
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
        String content = mapper.toContent(release);
        String hash = DigestUtils.sha256Hex(content);
        releases.insertIfAbsent(release.version(), release.publishedOn(), content, hash);
        String persistedHash = releases.findById(release.version()).orElseThrow().getContentHash();
        if (!hash.equals(persistedHash)) {
            throw new IllegalStateException("A versão legal " + release.version() + " já foi publicada. Crie uma nova versão.");
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Release publishNewRelease(Release release) {
        try { validate(release); }
        catch (IllegalStateException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha a versão, data, termos, privacidade e seções dos documentos.");
        }
        jdbc.sql("LOCK TABLE legal_document_release IN EXCLUSIVE MODE").update();
        if (release.version() != current().version() + 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Publique a próxima versão sequencial. Versões anteriores são imutáveis.");
        }
        if (mapper.toContent(release).length() > 1_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os documentos excedem o limite de 1 MB.");
        }
        publish(release);
        return release;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Release> releases() {
        return releases.findAllByOrderByVersionDesc().stream()
                .map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Release current() {
        return mapper.toDTO(releases.findFirstByOrderByVersionDesc().orElseThrow());
    }

    @Transactional(readOnly = true)
    public Release release(int version) {
        String content = releases.findById(version).map(LegalRelease::getContent)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versão não encontrada."));
        return mapper.fromContent(content);
    }

    @Transactional(readOnly = true)
    public Status status(String email) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        int currentVersion = releases.findFirstByOrderByVersionDesc().orElseThrow().getVersion();
        var acceptedAt = acceptances.findById(new LegalAcceptance.Id(user.getId(), currentVersion))
                .map(LegalAcceptance::getAcceptedAt);
        return new Status(currentVersion, acceptedAt.isEmpty(), acceptedAt.orElse(null));
    }

    @Transactional
    public Status accept(String email, int version) {
        var user = users.findForUpdateByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (version != current().version()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Os documentos foram atualizados. Leia a nova versão antes de confirmar.");
        }
        var id = new LegalAcceptance.Id(user.getId(), version);
        if (!acceptances.existsById(id)) {
            acceptances.saveAndFlush(new LegalAcceptance(user.getId(), version, Instant.now()));
        }
        return status(email);
    }

    @Transactional(readOnly = true)
    public List<Acceptance> history(String email) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return acceptances.findByIdUserIdOrderByIdVersionDesc(user.getId()).stream()
                .map(mapper::toDTO).toList();
    }
}
