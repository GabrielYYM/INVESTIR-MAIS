package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.controller.LessonController;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.education.service.LessonService;
import com.repositorio.investir_mais.domain.user.controller.UserController;
import com.repositorio.investir_mais.domain.token.controller.AccountTokenController;
import com.repositorio.investir_mais.domain.legal.controller.LegalDocumentsController;
import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import com.repositorio.investir_mais.domain.user.service.UserService;
import com.repositorio.investir_mais.infrastructure.exception.GlobalExceptionHandler;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {UserController.class, AccountTokenController.class, LessonController.class, LegalDocumentsController.class, AuditController.class, GlobalExceptionHandler.class})
@WebMvcTest({UserController.class, AccountTokenController.class, LessonController.class, LegalDocumentsController.class, AuditController.class})
@AutoConfigureMockMvc(addFilters = false)
class HttpValidationTest {
    @Autowired MockMvc mvc;
    @MockitoBean LessonService lessons;
    @MockitoBean UserService users;
    @MockitoBean CourseService courses;
    @MockitoBean(name = "registrationSessionAuthenticationStrategy") SessionAuthenticationStrategy sessionAuthentication;
    @MockitoBean SecurityContextRepository securityContexts;
    @MockitoBean LegalDocumentsService legal;
    @MockitoBean org.springframework.jdbc.core.simple.JdbcClient jdbc;
    @MockitoBean com.repositorio.investir_mais.domain.AuditAccessService auditAccess;

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"version\":0,\"publishedOn\":\"2020-01-01\",\"documents\":[]}",
            "{\"version\":2,\"publishedOn\":\"2020-01-01\",\"documents\":[null,null]}",
            "{\"version\":2,\"publishedOn\":\"2020-01-01\",\"documents\":[{\"id\":\"termos\",\"label\":\"Termos\",\"title\":\"Termos\",\"sections\":[null]},{\"id\":\"privacidade\",\"label\":\"Privacidade\",\"title\":\"Privacidade\",\"sections\":[{\"heading\":\"Título\",\"content\":\" \"}]}]}"})
    void invalidLegalDocumentsAreRejectedBeforeCallingService(String body) throws Exception {
        mvc.perform(post("/api/legal/versions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(legal);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=-1", "size=0", "size=101"})
    void auditBoundsUseNativeMvcValidation(String query) throws Exception {
        var parameter = query.split("=");
        mvc.perform(get("/api/audit").param(parameter[0], parameter[1]))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(jdbc);
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 100, 101})
    void newPasswordsHaveAMaximumOfOneHundredCharacters(int length) throws Exception {
        String password = "Aa1!" + "x".repeat(length - 4);
        when(users.registerUser(any())).thenReturn(new DefaultOneTimeToken("token", "user@investir.com", Instant.now().plusSeconds(300)));
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Teste","email":"user@investir.com","password":"%s"}
                        """.formatted(password)))
                .andExpect(status().is(length <= 100 ? 202 : 400));
        mvc.perform(post("/api/users/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token":"codigo","newPassword":"%s"}
                        """.formatted(password)))
                .andExpect(status().is(length <= 100 ? 200 : 400));
        mvc.perform(put("/api/users/update-password").principal(() -> "user@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"currentPassword":"antiga","newPassword":"%s"}
                        """.formatted(password)))
                .andExpect(status().is(length <= 100 ? 200 : 400));
        if (length > 100) verifyNoInteractions(users);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SENHAFORTE1!", "senhaforte1!", "SenhaForte!!", "SenhaForte12", "SenhaForte1 ", "SenhaForte1á", "Aa1!"})
    void weakPasswordsAreRejectedInAllCredentialCreationEndpoints(String password) throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Teste","email":"user@investir.com","password":"%s"}
                        """.formatted(password)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/users/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token":"codigo","newPassword":"%s"}
                        """.formatted(password)))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/users/update-password").principal(() -> "user@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"currentPassword":"antiga","newPassword":"%s"}
                        """.formatted(password)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(users);
    }

    @Test void strongNewPasswordsDoNotRequireTheCurrentPasswordToMeetTheNewPolicy() throws Exception {
        mvc.perform(put("/api/users/update-password").principal(() -> "user@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"currentPassword":"antiga","newPassword":"SenhaNova1!"}
                        """))
                .andExpect(status().isOk());
        mvc.perform(post("/api/users/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token":"codigo","newPassword":"SenhaNova1!"}
                        """))
                .andExpect(status().isOk());
        verify(users).updatePassword(any(), argThat(request -> request.currentPassword().equals("antiga")));
        verify(users).resetPassword(argThat(request -> request.newPassword().equals("SenhaNova1!")));
    }

    @Test void malformedJsonAndInvalidUuidKeepTheirNativeBadRequestStatus() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/users/not-a-uuid").principal(() -> "user@investir.com"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(users);
    }

    @Test void invalidRegistrationUsesNativeProblemDetailBeforeCallingService() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").isString())
                .andExpect(jsonPath("$.instance").value("/api/users/register"));
        verifyNoInteractions(users);
    }

    @Test void profileUpdateRequiresConfirmationPasswordBeforeCallingService() throws Exception {
        mvc.perform(put("/api/users/{id}", UUID.randomUUID()).principal(() -> "user@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Teste\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(users);
    }

    @Test void invalidLessonUrlsAreRejectedBeforeCallingService() throws Exception {
        mvc.perform(post("/api/lessons/course/{id}", UUID.randomUUID()).principal(() -> "teacher@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Aula válida\",\"description\":\"Descrição\",\"mediaUrl\":\"invalid\",\"thumbnailUrl\":\"invalid\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(lessons);
    }
    @Test void concurrentDuplicateRegistrationReturnsGenericAcceptance() throws Exception {
        var violation = new ConstraintViolationException("private SQL details",
                new SQLException("duplicate", "23505"),
                ConstraintViolationException.ConstraintKind.UNIQUE, "private_constraint");
        when(users.registerUser(any())).thenThrow(new DataIntegrityViolationException("private details", violation));
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Teste\",\"email\":\"test@example.com\",\"password\":\"Senha1234!\"}"))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.message").isString());
    }

    @Test void unknownIntegrityFailureRemainsInternalServerError() throws Exception {
        var lessonId = UUID.randomUUID();
        doThrow(new DataIntegrityViolationException("private details"))
                .when(lessons).deleteLesson(lessonId, "user@investir.com");
        mvc.perform(delete("/api/lessons/{lessonId}", lessonId).principal(() -> "user@investir.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro interno no servidor."));
    }

    @Test void auditIdentifierCollisionIsNotReportedAsDuplicateProfileData() throws Exception {
        var violation = new ConstraintViolationException("private SQL details",
                new SQLException("duplicate revision", "23505"),
                ConstraintViolationException.ConstraintKind.UNIQUE, "revinfo_pkey");
        when(users.updateProfile(any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("private details", violation));
        mvc.perform(put("/api/users/{id}", UUID.randomUUID()).principal(() -> "user@investir.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Teste","email":"user@investir.com","currentPassword":"Senha1234"}
                        """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro interno no servidor."));
    }
    @Test void foreignKeyConflictReturns409() throws Exception {
        var violation = new ConstraintViolationException("private SQL details",
                new SQLException("foreign key", "23503"),
                ConstraintViolationException.ConstraintKind.FOREIGN_KEY, "private_constraint");
        var lessonId = UUID.randomUUID();
        doThrow(new DataIntegrityViolationException("private details", violation))
                .when(lessons).deleteLesson(lessonId, "user@investir.com");
        mvc.perform(delete("/api/lessons/{lessonId}", lessonId).principal(() -> "user@investir.com"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A operação conflita com registros relacionados."));
    }
    @Test
    void registrationWithoutAgeOrBirthDateIsAccepted() throws Exception {
        when(users.registerUser(any())).thenReturn(new DefaultOneTimeToken(
                "registration-token", "test@example.com", Instant.now().plusSeconds(300)));
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Teste\",\"email\":\"test@example.com\",\"password\":\"Senha1234!\"}"))
                .andExpect(status().isAccepted());
        verify(users).registerUser(argThat(request -> request.name().equals("Teste")));
    }
}
