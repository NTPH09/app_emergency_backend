package com.emergency.backend.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProviderConverter implements AttributeConverter<Provider, String> {

    @Override
    public String convertToDatabaseColumn(Provider provider) {
        return provider == null ? null : provider.name().toLowerCase();
    }

    @Override
    public Provider convertToEntityAttribute(String value) {
        return value == null ? null : Provider.valueOf(value.toUpperCase());
    }
}