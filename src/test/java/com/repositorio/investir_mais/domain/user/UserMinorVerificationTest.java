package com.repositorio.investir_mais.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.UserSecurity;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;

class UserMinorVerificationTest {

    @Test
    @DisplayName("Deve identificar corretamente se o usuário tem menos de 12 anos")
    void testIsUnder12() {
        LocalDate minorBirthDate = LocalDate.now().minusYears(10);
        User childUser = User.builder()
                .name("Crianca Teste")
                .email("crianca@email.com")
                .birthDate(minorBirthDate)
                .guardianEmail("responsavel@email.com")
                .build();

        assertTrue(childUser.isUnder12(), "Usuário com 10 anos deve ser considerado menor de 12 anos");

        LocalDate adultBirthDate = LocalDate.now().minusYears(15);
        User teenUser = User.builder()
                .name("Jovem Teste")
                .email("jovem@email.com")
                .birthDate(adultBirthDate)
                .build();

        assertFalse(teenUser.isUnder12(), "Usuário com 15 anos não deve ser menor de 12 anos");
    }

    @Test
    @DisplayName("Deve gerar e limpar códigos de verificação para o usuário e para o responsável")
    void testGenerateAndClearVerificationCodes() {
        UserSecurity security = UserSecurity.builder()
                .password("hash123")
                .role(UserRole.ALUNO)
                .emailHash("hashEmail")
                .emailVerified(false)
                .build();

        String childCode = "123456";
        String guardianCode = "654321";
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        security.generateVerificationCodes(childCode, guardianCode, expiry);

        assertEquals("123456", security.getVerificationCode());
        assertEquals("654321", security.getGuardianVerificationCode());
        assertNotNull(security.getVerificationExpiry());
        assertFalse(security.isEmailVerified());

        // Ativa a conta e limpa os códigos
        security.setEmailVerified(true);
        security.clearVerificationCodes();

        assertTrue(security.isEmailVerified());
        assertNull(security.getVerificationCode());
        assertNull(security.getGuardianVerificationCode());
        assertNull(security.getVerificationExpiry());
    }
}
