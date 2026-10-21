package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.user.controller.UserController;
import com.repositorio.investir_mais.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({BrapiQuoteController.class, UserController.class})
@AutoConfigureMockMvc(addFilters = false)
class HttpValidationTest {
    @Autowired MockMvc mvc;
    @MockitoBean AssetQuoteClient quotes;
    @MockitoBean UserService users;

    @Test void invalidTickerIsRejectedBeforeCallingBrapi() throws Exception {
        mvc.perform(get("/api/assets/INVALID_TICKER/quote"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(quotes);
    }

    @Test void validTickerIsNormalizedAndReturned() throws Exception {
        when(quotes.getQuote("PETR4")).thenReturn("{}");
        mvc.perform(get("/api/assets/petr4/quote"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
        verify(quotes).getQuote("PETR4");
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
    @Test void uniqueConstraintConflictReturns409WithoutLeakingDatabaseDetails() throws Exception {
        var violation = new org.hibernate.exception.ConstraintViolationException("private SQL details",
                new java.sql.SQLException("duplicate", "23505"),
                org.hibernate.exception.ConstraintViolationException.ConstraintKind.UNIQUE, "private_constraint");
        when(users.registerUser(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("private details", violation));
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Teste\",\"age\":22,\"email\":\"test@example.com\",\"password\":\"Senha1234\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Já existe um registro com esses dados."));
    }

    @Test void unknownIntegrityFailureRemainsInternalServerError() throws Exception {
        when(quotes.getQuote("PETR4")).thenThrow(new org.springframework.dao.DataIntegrityViolationException("private details"));
        mvc.perform(get("/api/assets/PETR4/quote"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro interno no servidor."));
    }
    @Test void foreignKeyConflictReturns409() throws Exception {
        var violation = new org.hibernate.exception.ConstraintViolationException("private SQL details",
                new java.sql.SQLException("foreign key", "23503"),
                org.hibernate.exception.ConstraintViolationException.ConstraintKind.FOREIGN_KEY, "private_constraint");
        when(quotes.getQuote("PETR4")).thenThrow(new org.springframework.dao.DataIntegrityViolationException("private details", violation));
        mvc.perform(get("/api/assets/PETR4/quote"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A operação conflita com registros relacionados."));
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {-1, 121})
    void ageOutsideRangeIsRejectedBeforeRegistration(int age) throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Teste\",\"age\":" + age + ",\"email\":\"test@example.com\",\"password\":\"Senha1234\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(users);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {0, 120})
    void boundaryAgesAreAccepted(int age) throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Teste\",\"age\":" + age + ",\"email\":\"test@example.com\",\"password\":\"Senha1234\"}"))
                .andExpect(status().isCreated());
        verify(users).registerUser(org.mockito.ArgumentMatchers.argThat(request -> request.age() == age));
    }
}
