package com.ems.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

/**
 * Utility class for JSON operations in tests
 */
public class JsonUtil {
    private static final ObjectMapper mapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Convert an object to JSON string
     */
    public static String toJson(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }

    /**
     * Create a deep copy of an object by converting to JSON and back
     */
    public static <T> T deepCopy(T object, Class<T> clazz) throws IOException {
        return fromJson(toJson(object), clazz);
    }

    /**
     * Check if a string is valid JSON
     */
    public static boolean isValidJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Convert JSON string to pretty-printed format
     */
    public static String toPrettyJson(String json) throws IOException {
        Object jsonObject = mapper.readValue(json, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    }

    /**
     * Convert object to pretty-printed JSON string
     */
    public static String toPrettyJson(Object object) throws IOException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    /**
     * Get ObjectMapper instance
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }
}
