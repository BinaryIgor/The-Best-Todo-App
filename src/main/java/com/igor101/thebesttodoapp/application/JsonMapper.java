package com.igor101.thebesttodoapp.application;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JsonMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> toObjects(String json, Class<T> type) {
        try {
            var collectionType = MAPPER.getTypeFactory().constructCollectionType(List.class, type);
            return MAPPER.readValue(json, collectionType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
