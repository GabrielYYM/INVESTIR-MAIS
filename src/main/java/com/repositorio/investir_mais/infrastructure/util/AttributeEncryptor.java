package com.repositorio.investir_mais.infrastructure.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.repositorio.investir_mais.common.constants.MessageConstants;

@Component
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private static final int AES_KEY_BITS = 256;

    private final Key key;
    private final SecureRandom secureRandom;

    public AttributeEncryptor(
            @Value("${api.security.db.encryption.key:#{null}}") String secretKey,
            @Value("${security.aes.kdf.salt}") String kdfSalt,
            @Value("${security.aes.kdf.iterations}") int kdfIterations) {

        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException(MessageConstants.Infrastructure.ERR_ENCRYPTION_KEY_INVALID);
        }
        if (kdfSalt == null || kdfSalt.isBlank()) {
            throw new IllegalArgumentException(MessageConstants.Infrastructure.ERR_KDF_SALT_INVALID);
        }

        try {
            PBEKeySpec spec = new PBEKeySpec(
                secretKey.toCharArray(),
                kdfSalt.getBytes(StandardCharsets.UTF_8),
                kdfIterations,
                AES_KEY_BITS
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey derivedKey = factory.generateSecret(spec);
            spec.clearPassword();
            this.key = new SecretKeySpec(derivedKey.getEncoded(), ALGORITHM);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageConstants.Infrastructure.ERR_KDF_DERIVATION_FAILED, e);
        }

        this.secureRandom = new SecureRandom();
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            
            byte[] cipherText = cipher.doFinal(attribute.getBytes());
            
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new IllegalStateException(MessageConstants.Infrastructure.ERR_ENCRYPT_FAILED, e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] decodedData = Base64.getDecoder().decode(dbData);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, iv.length);
            
            byte[] cipherText = new byte[decodedData.length - GCM_IV_LENGTH];
            System.arraycopy(decodedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            
            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            throw new IllegalStateException(MessageConstants.Infrastructure.ERR_DECRYPT_FAILED, e);
        }
    }
}