package com.ridemate.ridemate_server.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

@Converter(autoApply = false)
public class VectorAttributeConverter implements AttributeConverter<String, Object> {

    @Override
    public Object convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.trim().isEmpty()) {
            return null;
        }
        
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("vector");
            pgObject.setValue(attribute);
            return pgObject;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Failed to convert string to vector: " + attribute, e);
        }
    }

    @Override
    public String convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        
        if (dbData instanceof PGobject) {
            PGobject pgObject = (PGobject) dbData;
            return pgObject.getValue();
        }
        
        return dbData.toString();
    }
}
