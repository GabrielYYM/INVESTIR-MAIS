package com.repositorio.investir_mais.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.repositorio.investir_mais.common.constants.MessageConstants;


@Service
public class CryptoService {

    @Value("${api.security.token.secret}")
    private String tokenSecret;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSha256Hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    value.toLowerCase().getBytes(StandardCharsets.UTF_8)
            );
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

    public String generateHmacTokenHash(String token) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(
                    tokenSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(MessageConstants.Auth.ERR_HASH_TOKEN, e);
        }
    }

    public String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }


    public String generateNumericCode(int length) {
        return String.format(
                "%0" + length + "d",
                secureRandom.nextInt((int) Math.pow(10, length))
        );
    }
}