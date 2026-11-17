package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.legal.DTO.LegalDocumentDTO.*;
import com.repositorio.investir_mais.domain.legal.mapper.LegalDocumentMapper;
import com.repositorio.investir_mais.domain.legal.repository.LegalAcceptanceRepository;
import com.repositorio.investir_mais.domain.legal.repository.LegalReleaseRepository;
import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LegalDocumentsValidationTest {
    @Test
    void bundledFilesAndDirectPublicationAlsoValidateNestedContent() {
        var releases = mock(LegalReleaseRepository.class);
        var mapper = mock(LegalDocumentMapper.class);
        var invalid = new Release(1, LocalDate.of(2020, 1, 1), List.of(
                new Document("termos", "Termos", "Termos de Uso", List.of(new Section("Título", " "))),
                new Document("privacidade", "Privacidade", "Política", List.of(new Section("Título", "Texto")))));
        when(mapper.fromContent(anyString())).thenReturn(invalid);
        try (var validation = Validation.buildDefaultValidatorFactory()) {
            var service = new LegalDocumentsService(mock(JdbcClient.class), releases,
                    mock(LegalAcceptanceRepository.class), mock(UserRepository.class), mapper, validation.getValidator());
            assertThrows(IllegalStateException.class, service::publishBundledReleases);
            var failure = assertThrows(ResponseStatusException.class, () -> service.publishNewRelease(invalid));
            assertEquals(400, failure.getStatusCode().value());
            verifyNoInteractions(releases);
        }
    }
}
