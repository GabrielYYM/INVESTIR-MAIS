package com.repositorio.investirMais.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.repositorio.investirMais.common.constants.MessageConstants;

/**
 * Serviço responsável pelas operações criptográficas do domínio de autenticação
 * e outras áreas.
 */
@Service
public class CryptoService {

    @Value("${api.security.token.secret}")
    private String tokenSecret;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Gera um hash determinístico de um valor em texto (ex: e-mail)
     * utilizando SHA-256.
     * 
     * @param value O valor a ser hasheado (normalmente normalizado).
     * @return String hexadecimal correspondente ao hash.
     */
    public String generateSha256Hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    value.toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hashBytes.length);

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception _) {
            throw new RuntimeException(
                    MessageConstants.Auth.ERR_HASH_EMAIL);
        }
    }

    /**
     * Gera um HMAC-SHA256 codificado em Base64Url para uso com tokens de segurança.
     * 
     * @param token O token bruto.
     * @return HMAC hash em Base64Url sem padding.
     */
    public String generateHmacTokenHash(String token) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(
                    tokenSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(MessageConstants.Auth.ERR_HASH_TOKEN, e);
        }
    }

    /**
     * Gera um token aleatório seguro usando SecureRandom.
     * 
     * @return String contendo um token Base64Url-encoded de 32 bytes de entropia.
     */
    public String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    /**
     * Gera um código numérico aleatório de comprimento variável.
     * Utiliza SecureRandom para garantir que os códigos sejam criptograficamente
     * fortes.
     * 
     * @param length Quantidade de dígitos no código gerado.
     * @return String formatada com o código numérico.
     */
    public String generateNumericCode(int length) {
        return String.format(
                "%0" + length + "d",
                secureRandom.nextInt((int) Math.pow(10, length)));
    }
}
