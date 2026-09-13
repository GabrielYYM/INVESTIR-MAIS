package com.repositorio.investir_mais.infrastructure.util;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.springframework.stereotype.Component;

@Component
@Converter
public class BigDecimalEncryptor implements AttributeConverter<BigDecimal, String> {

    private final AttributeEncryptor stringEncryptor;

    public BigDecimalEncryptor(AttributeEncryptor stringEncryptor) {
        this.stringEncryptor = stringEncryptor;
    }

    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) {
            return null;
        }
        return stringEncryptor.convertToDatabaseColumn(attribute.toString());
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        String decrypted = stringEncryptor.convertToEntityAttribute(dbData);
        if (decrypted == null) {
            return null;
        }
        return new BigDecimal(decrypted);
    }
}
