package com.example.sensor.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;

@Converter(autoApply = true)
public class InstantAttributeConverter implements AttributeConverter<Instant, Timestamp> {
  @Override
  public Timestamp convertToDatabaseColumn(Instant attribute) {
    return attribute == null ? null : Timestamp.from(attribute);
  }

  @Override
  public Instant convertToEntityAttribute(Timestamp dbData) {
    return dbData == null ? null : dbData.toInstant();
  }
}
